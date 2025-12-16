package tschipp.carryon.compat;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tschipp.carryon.config.AnnotationData;
import tschipp.carryon.config.BuiltCategory;
import tschipp.carryon.config.BuiltConfig;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public record ClothConfigCompat(BuiltConfig client, BuiltConfig common, Runnable onSave) {

    public static Screen getConfigScreen(BuiltConfig client, BuiltConfig common, Screen parentScreen, Runnable onSave) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Component.translatable("key.carry.category"));

        buildConfigType(client, builder, "Client Config");
        buildConfigType(common, builder, "Common Config");

        builder.setSavingRunnable(onSave);

        return builder.build();
    }

    private static void buildConfigType(BuiltConfig cfg, ConfigBuilder builder, String name) {
        ConfigCategory configCategory = builder.getOrCreateCategory(Component.literal(name));
        buildProperties(cfg, configCategory, builder, null);
    }


    private static void buildCategory(BuiltCategory category, ConfigCategory categoryBuilder, ConfigBuilder builder) {
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        SubCategoryBuilder subBuilder = entryBuilder.startSubCategory(Component.translatable(category.translation));

        buildProperties(category, categoryBuilder, builder, subBuilder);
    }

    private static void buildProperties(BuiltCategory category, ConfigCategory categoryBuilder, ConfigBuilder builder, @Nullable SubCategoryBuilder subBuilder) {
        category.categories.forEach(cat -> {
            buildCategory(cat, categoryBuilder, builder);
        });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        category.properties.forEach(propertyData -> {
            try {
                AnnotationData data = propertyData.getData();
                var entry =
                        switch (data.type()) {
                            case BOOLEAN ->
                                    entryBuilder.startBooleanToggle(Component.literal(propertyData.getField().getName()), propertyData.getBoolean())
                                            .setTooltip(Component.literal(data.description()))
                                            .setDefaultValue(propertyData.getDefaultBoolean())
                                            .setSaveConsumer((Consumer<Boolean>) propertyData.getSetter())
                                            .build();
                            case INT ->
                                    entryBuilder.startIntField(Component.literal(propertyData.getField().getName()), propertyData.getInt())
                                            .setTooltip(Component.literal(data.description()))
                                            .setDefaultValue(propertyData.getDefaultInt())
                                            .setMin(data.min())
                                            .setMax(data.max())
                                            .setSaveConsumer((Consumer<Integer>) propertyData.getSetter())
                                            .build();
                            case DOUBLE ->
                                    entryBuilder.startDoubleField(Component.literal(propertyData.getField().getName()), propertyData.getDouble())
                                            .setTooltip(Component.literal(data.description()))
                                            .setDefaultValue(propertyData.getDefaultDouble())
                                            .setMin(data.minD())
                                            .setMax(data.maxD())
                                            .setSaveConsumer((Consumer<Double>) propertyData.getSetter())
                                            .build();
                            case STRING_ARRAY ->
                                    entryBuilder.startStrList(Component.literal(propertyData.getField().getName()), List.of(propertyData.getStringArray()))
                                            .setTooltip(Component.literal(data.description()))
                                            .setInsertInFront(true)
                                            .setDefaultValue(List.of(propertyData.getDefaultStringArray()))
                                            .setCellErrorSupplier(str -> str.matches(data.validationRegex()) ? Optional.empty() : Optional.of(Component.literal("Invalid Format")))
                                            .setSaveConsumer((Consumer<List<String>>) propertyData.getSetter())
                                            .build();
                            default -> null;
                        };

                if(subBuilder == null)
                    categoryBuilder.addEntry(entry);
                else
                    subBuilder.add(entry);

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        if(subBuilder != null)
            categoryBuilder.addEntry(subBuilder.build());
    }
}



