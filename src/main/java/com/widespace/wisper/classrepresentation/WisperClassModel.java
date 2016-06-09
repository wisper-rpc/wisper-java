package com.widespace.wisper.classrepresentation;

import com.widespace.wisper.base.Wisper;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * This is a model object for representation of a class in RPC.
 * The class may contain static and instance methods.
 * <p/>
 * Created by Ehssan Hoorvash on 21/05/14.
 */
public class WisperClassModel
{
    private String mapName;
    private HashMap<String, WisperMethod> instanceMethods;
    private HashMap<String, WisperMethod> staticMethods;
    private HashMap<String, WisperProperty> properties;

    private Class<? extends Wisper> classRef;

    private WisperClassModel()
    {
        instanceMethods = new HashMap<String, WisperMethod>();
        staticMethods = new HashMap<String, WisperMethod>();
        properties = new HashMap<String, WisperProperty>();
    }

    public WisperClassModel(Wisper object, String mappingName)
    {
        this();
        this.classRef = object.getClass();
        this.mapName = mappingName;
    }

    public WisperClassModel(Class<? extends Wisper> clazz, String mappingName)
    {
        this();
        this.classRef = clazz;
        this.mapName = mappingName;
    }

    public WisperClassModel(Class<? extends Wisper> clazz)
    {
        this();
        this.classRef = clazz;
    }

    /**
     * Adds an instance method to the class implementation.
     *
     * @param method is the representation of the RPC instance method.
     */
    public void addInstanceMethod(@NotNull WisperMethod method)
    {
        instanceMethods.put(method.getMapName(), method);
    }

    /**
     * Adds a static method to the class implementation.
     *
     * @param method is the representation of the RPC instance method.
     */
    public void addStaticMethod(@NotNull WisperMethod method)
    {
        staticMethods.put(method.getMapName(), method);
    }

    /**
     * Lists the instance methods.
     *
     * @return A HashMap of instance methods with mapping name as the key and the WisperMethod instance as the value.
     */
    public @NotNull HashMap<String, WisperMethod> getInstanceMethods()
    {
        return instanceMethods;
    }

    /**
     * Lists the static methods.
     *
     * @return A HashMap of static methods with mapping name as the key and the WisperMethod instance as the value.
     */
    public @NotNull HashMap<String, WisperMethod> getStaticMethods()
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
    public Class<? extends Wisper> getClassRef()
    {
        return  classRef;
    }

    /**
     * Adds an WisperProperty to the class model
     *
     * @param property
     */
    public void addProperty(WisperProperty property)
    {
        properties.put(property.getMappingName(), property);
    }

    /**
     * Returns the list of RPCClassProperties registered to this class
     *
     * @return hash map of properties
     */
    public HashMap<String, WisperProperty> getProperties()
    {
        return properties;
    }

    public void setMapName(String mapName)
    {
        this.mapName = mapName;
    }

    public void setClassRef(Class<? extends Wisper> classRef)
    {
        this.classRef = classRef;
    }
}
