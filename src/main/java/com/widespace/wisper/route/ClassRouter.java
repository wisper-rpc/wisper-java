package com.widespace.wisper.route;


import com.widespace.wisper.annotations.RPCClassRegistry;
import com.widespace.wisper.base.Wisper;
import com.widespace.wisper.classrepresentation.WisperClassModel;
import org.jetbrains.annotations.NotNull;

public class ClassRouter extends Router
{
    private WisperClassModel wisperClassModel;

    public ClassRouter(@NotNull Class<? extends Wisper> clazz)
    {
        wisperClassModel = RPCClassRegistry.register(clazz);
    }

    public WisperClassModel getWisperClassModel()
    {
        return wisperClassModel;
    }
}
