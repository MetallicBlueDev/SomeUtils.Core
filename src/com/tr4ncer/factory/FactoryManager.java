package com.tr4ncer.factory;

import com.tr4ncer.core.*;
import com.tr4ncer.logger.*;
import com.tr4ncer.utils.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Gestionnaire d'instance.
 *
 * @version 3.00.00
 * @author Sebastien Villemain
 */
public class FactoryManager {

    /**
     * Cache des instances de gestionnaire.
     */
    private static final Map<Class<? extends EntityProcess>, Map<String, EntityProcess>> MANAGERS = new ConcurrentHashMap<>();

    /**
     * Registre des procédures enregistrés.
     */
    private static final Map<Class<? extends EntityProcess>, Class<? extends EntityProcess>> REGISTERED_PROCEDURES = new ConcurrentHashMap<>();

    private static boolean disposing = false;

    private FactoryManager() {
        // NE RIEN FAIRE
    }

    public static boolean isDisposing() {
        return disposing;
    }

    /**
     * Ajoute la procédure dans le registre.
     * Permet de spécifier l'entité à instancier pour un super type.
     *
     * @param process
     */
    public static void register(Class<? extends EntityProcess> process) {
        if (process != null) {
            Class<? extends EntityProcess> superClass = getSuperclass(process);
            REGISTERED_PROCEDURES.put(superClass, process);
        }
    }

    /**
     * Retourne l'instance du gestionnaire nommé.
     * Si aucune instance disponible, retourne
     * <code>null</code>.
     *
     * @param <E>
     * @param type
     * @param name
     * @return E or <code>null</code>.
     */
    public static <E extends EntityProcess> E getInstance(Class<E> type, String name) {
        E manager = null;

        if (type != null) {
            Class<? extends EntityProcess> superClass = getSuperclass(type);
            manager = getManagerCached(superClass, type, name);

            if (manager == null) {
                // Recherche le nom alternatif généré
                manager = getManagerCached(superClass, type, getAlternativeName(name, type));
            }

            if (manager == null) {
                if (canCreateInstance(type)) {
                    manager = makeInstance(type);
                }

                if (manager != null) {
                    if (!MANAGERS.containsKey(superClass)) {
                        MANAGERS.put(superClass, new ConcurrentHashMap<>());
                    }

                    Map<String, EntityProcess> managersMap = MANAGERS.get(superClass);

                    if (managersMap.containsKey(name)) {
                        name = getAlternativeName(name, type);
                    }

                    managersMap.put(name, manager);
                    manager.createProcess();

                    if (canUseLogging()) {
                        LoggerManager.getInstance().addDebug("Ready to use " + getFullName(superClass, null, manager) + ".");
                    }
                } else {
                    if (canUseLogging()) {
                        LoggerManager.getInstance().addDebug("Unable to load class " + type.toString() + ".");
                    }
                }
            }
        }
        return manager;
    }

    /**
     * Retourne l'instance du gestionnaire par défaut.
     * Si aucune instance disponible, retourne
     * <code>null</code>.
     *
     * @param <E>
     * @param type
     * @return
     */
    public static <E extends EntityProcess> E getInstance(Class<E> type) {
        return getInstance(type, "default");
    }

    /**
     * Retourne le nom assigné au gestionnaire.
     *
     * @param entity
     * @return
     */
    public static String getName(EntityProcess entity) {
        String name = null;

        if (entity != null) {
            Class<? extends EntityProcess> superclass = getSuperclass(entity.getClass());

            if (MANAGERS.containsKey(superclass)) {
                for (Map.Entry<String, EntityProcess> manager : MANAGERS.get(superclass).entrySet()) {
                    if (manager.getValue().equals(entity)) {
                        name = manager.getKey();
                        break;
                    }
                }
            }
        }

        if (name == null) {
            name = (entity != null ? entity.getClass().getSimpleName() : "");
        }
        return name;
    }

    /**
     * Retourne toutes les instances gérées.
     *
     * @return
     */
    public static List<EntityProcess> getInstances() {
        List<EntityProcess> processes = new ArrayList<>();

        for (Map<String, EntityProcess> entities : MANAGERS.values()) {
            processes.addAll(entities.values());
        }
        return processes;
    }

    /**
     * Destruction du gestionnaire et de ses ressources.
     *
     * @param <E>
     * @param entity
     * @return
     */
    public static <E extends EntityProcess> E dispose(E entity) {
        boolean success = false;

        if (entity != null) {
            Class<? extends EntityProcess> superClass = getSuperclass(entity.getClass());

            if (canUseLogging()) {
                LoggerManager.getInstance().addDebug("Destroy " + getFullName(superClass, null, entity) + ".");
            }

            entity.destroyProcess();

            if (MANAGERS.containsKey(superClass)) {
                for (Iterator<EntityProcess> it = MANAGERS.get(superClass).values().iterator(); it.hasNext();) {
                    EntityProcess manager = it.next();

                    if (manager.equals(entity)) {
                        it.remove();
                        success = true;
                        break;
                    }
                }

                if (MANAGERS.get(superClass).isEmpty()) {
                    MANAGERS.remove(superClass);
                }
            }
        }
        return (success) ? null : entity;
    }

    /**
     * Destruction du gestionnaire et de ses ressources suivant son type et son nom.
     *
     * @param type
     * @param name
     */
    public static void dispose(Class<? extends EntityProcess> type, String name) {
        if (type != null) {
            Class<? extends EntityProcess> superClass = getSuperclass(type);

            if (MANAGERS.containsKey(superClass) && MANAGERS.get(superClass).containsKey(name)) {
                dispose(MANAGERS.get(superClass).get(name));
            }
        }
    }

    /**
     * Destruction des gestionnaires et de leurs ressources suivant le type.
     *
     * @param type
     */
    public static void dispose(Class<? extends EntityProcess> type) {
        if (type != null) {
            Class<? extends EntityProcess> superClass = getSuperclass(type);

            if (MANAGERS.containsKey(superClass)) {
                EntityProcess[] entities = MANAGERS.get(superClass).values().toArray(new EntityProcess[MANAGERS.get(superClass).size()]);

                // Destruction en ordre inverse
                for (int i = entities.length - 1; i >= 0; i--) {
                    dispose(entities[i]);
                }
            }
        }
    }

    /**
     * Destruction de tous les gestionnaires et de leurs ressources.
     */
    public static void dispose() {
        disposing = true;

        List<EntityProcess> entities = new ArrayList<>();

        for (Map<String, EntityProcess> map : MANAGERS.values()) {
            entities.addAll(map.values());
        }

        Collections.sort(entities, new ManagerComparator());

        // Destruction en ordre inverse
        for (int i = entities.size() - 1; i >= 0; i--) {
            dispose(entities.get(i));
        }
    }

    /**
     * Vérifie si une instanciation du gestionnaire est possible.
     *
     * @param type Le type de ressource.
     * @return
     */
    public static boolean canCreateInstance(Class<? extends EntityProcess> type) {
        boolean canCreate = false;

        if (type != null) {
            String classPath = getClass(type);
            canCreate = canCreateInstance(classPath);
        }
        return canCreate;
    }

    /**
     * Détermine si une instance du type demandé a été créé.
     *
     * @param type
     * @return
     */
    public static boolean hasInstance(Class<? extends EntityProcess> type) {
        return type != null && MANAGERS.containsKey(getSuperclass(type));
    }

    /**
     * Retourne le chemin par défaut du gestionnaire.
     *
     * @param type Le type de ressource.
     * @return
     */
    public static String getClass(Class<? extends EntityProcess> type) {
        String classPath = null;

        // Tentative de recherche dans le registre
        if (!REGISTERED_PROCEDURES.isEmpty()) {
            Class<? extends EntityProcess> superClass = getSuperclass(type);

            if (REGISTERED_PROCEDURES.containsKey(superClass)) {
                classPath = REGISTERED_PROCEDURES.get(superClass).getCanonicalName();
            }
        }
        if (classPath == null
            && !type.isInterface()
            && !Modifier.isAbstract(type.getModifiers())) {
            classPath = type.getCanonicalName();
        }
        return classPath;
    }

    /**
     * Retourne le gestionnaire correspondant contenu dans le cache.
     *
     * @param <E>
     * @param superClass
     * @param type
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <E extends EntityProcess> E getManagerCached(Class<? extends EntityProcess> superClass, Class<E> type, String name) {
        E manager = null;

        if (MANAGERS.containsKey(superClass)
            && MANAGERS.get(superClass).containsKey(name)) {
            if (type.isInstance(MANAGERS.get(superClass).get(name))) {
                manager = (E) MANAGERS.get(superClass).get(name);
            }
        }
        return manager;
    }

    /**
     * Retourne un nom alternatif généré en cas de conflit.
     *
     * @param <E>
     * @param name
     * @param type
     * @return
     */
    private static <E extends EntityProcess> String getAlternativeName(String name, Class<E> type) {
        return name + ">" + type.getCanonicalName();
    }

    /**
     * Retourne le nom du type en cours.
     *
     * @param superClass
     * @param type
     * @param entity
     * @return
     */
    private static String getFullName(Class<? extends EntityProcess> superClass, Class<? extends EntityProcess> type, EntityProcess entity) {
        String typeName;

        if (entity != null) {
            typeName = entity.getClass().getSimpleName();
        } else if (type != null) {
            typeName = type.getSimpleName();
        } else {
            typeName = "";
        }

        String superName;

        if (superClass != null) {
            superName = superClass.getSimpleName();
        } else if (type != null) {
            superName = getSuperclass(type).getSimpleName();
        } else {
            superName = "";
        }

        String managerName;

        if (entity != null) {
            managerName = entity.getName();
        } else if (type != null) {
            managerName = type.getCanonicalName();
        } else {
            managerName = "";
        }
        return (!typeName.equalsIgnoreCase(superName) ? typeName + "::" + superName : typeName)
               + (!typeName.equalsIgnoreCase(managerName) ? "/" + managerName : "");
    }

    /**
     * Création d'une nouvelle instance du gestionnaire.
     *
     * @param type Le type de ressource.
     * @return
     */
    @SuppressWarnings("unchecked")
    private static <E extends EntityProcess> E makeInstance(Class<? extends EntityProcess> type) {
        if (canUseLogging()) {
            LoggerManager.getInstance().addDebug("Make new instance " + getFullName(null, type, null) + ".");
        }

        EntityProcess instance = null;
        Class<? extends EntityProcess> entityType = PackagesHelper.getClass(getClass(type));

        if (entityType != null) {
            instance = PackagesHelper.makeInstance(entityType);
        }
        return (E) instance;
    }

    /**
     * Vérifie si une instanciation est possible.
     *
     * @param classPath
     * @return
     */
    private static boolean canCreateInstance(String classPath) {
        boolean result = false;

        try {
            result = PackagesHelper.getClass(classPath) != null;
        } catch (Exception ex) {
            LoggerManager.getInstance().addError(ex);
        }
        return result;
    }

    /**
     * Retourne le classe de base pour le type demandé.
     *
     * @param type
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends EntityProcess> getSuperclass(Class<? extends EntityProcess> type) {
        Class<? extends EntityProcess> superClass = type;

        if (type.isInterface()
            && !EntityProcess.class.equals(type)) {
            for (Class<? extends EntityProcess> aClass : REGISTERED_PROCEDURES.values()) {
                if (type.isAssignableFrom(aClass)) {
                    superClass = aClass;
                    break;
                }
            }
        }
        while (superClass.getSuperclass() != null
               && EntityProcess.class.isAssignableFrom(superClass.getSuperclass())) {
            superClass = (Class<? extends EntityProcess>) superClass.getSuperclass();
        }
        return superClass;
    }

    private static boolean canUseLogging() {
        return hasInstance(MainManager.class) && hasInstance(LoggerManager.class);
    }

    private static class ManagerComparator implements Comparator<EntityProcess>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(EntityProcess o1, EntityProcess o2) {
            int rslt = 0;

            if (LoggerNotificator.class.isAssignableFrom(o1.getClass())) {
                rslt = -1;
            } else if (LoggerManager.class.isAssignableFrom(o1.getClass())) {
                if (LoggerNotificator.class.isAssignableFrom(o2.getClass())) {
                    rslt = 1;
                } else {
                    rslt = -1;
                }
            }
            return rslt;
        }
    }
}
