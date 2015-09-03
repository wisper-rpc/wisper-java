package com.widespace.wisper.classrepresentation;

import java.util.HashMap;

/**
 * This is a model object for representation of a class in RPC.
 * The class may contain static and instance methods.
 * <p/>
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public class RPCClass
{
    private String mapName;
    private HashMap<String, RPCClassMethod> instanceMethods;
    private HashMap<String, RPCClassMethod> staticMethods;
    private HashMap<String, RPCClassProperty> properties;

    private Class<?> classRef;

    private RPCClass()
    {
        instanceMethods = new HashMap<String, RPCClassMethod>();
        staticMethods = new HashMap<String, RPCClassMethod>();
        properties = new HashMap<String, RPCClassProperty>();
    }

    public RPCClass(Object object, String mappingName)
    {
        this();
        this.classRef = object.getClass();
        this.mapName = mappingName;
    }

    public RPCClass(Class<?> clazz, String mappingName)
    {
        this();
        this.classRef = clazz;
        this.mapName = mappingName;
    }

    /**
     * Adds an instance method to the class implementation.
     *
     * @param method is the representation of the RPC instance method.
     */
    public void addInstanceMethod(RPCClassMethod method)
    {
        instanceMethods.put(method.getMapName(), method);
    }

    /**
     * Adds a static method to the class implementation.
     *
     * @param method is the representation of the RPC instance method.
     */
    public void addStaticMethod(RPCClassMethod method)
    {
        staticMethods.put(method.getMapName(), method);
    }

    /**
     * Lists the instance methods.
     *
     * @return A HashMap of instance methods with mapping name as the key and the RPCClassMethod instance as the value.
     */
    public HashMap<String, RPCClassMethod> getInstanceMethods()
    {
        return instanceMethods;
    }

    /**
     * Lists the static methods.
     *
     * @return A HashMap of static methods with mapping name as the key and the RPCClassMethod instance as the value.
     */
    public HashMap<String, RPCClassMethod> getStaticMethods()
    {
        return staticMethods;
    }

    /**
     * returns the mapping name used for this class.
     *
     * @return a String representing the mapping name.
     */
    public String getMapName()
    {
        return mapName;
    }

    /**
     * Returns a reference to the actual class for which this model object has been built
     *
     * @return a Class object representing the actual modeled Java class.
     */
    public Class<?> getClassRef()
    {
        return classRef;
    }

    /**
     * Adds an RPCClassProperty to the class model
     *
     * @param property
     */
    public void addProperty(RPCClassProperty property)
    {
        properties.put(property.getMappingName(), property);
    }

    /**
     * Returns the list of RPCClassProperties registered to this class
     *
     * @return hash map of properties
     */
    public HashMap<String, RPCClassProperty> getProperties()
    {
        return properties;
    }
}
