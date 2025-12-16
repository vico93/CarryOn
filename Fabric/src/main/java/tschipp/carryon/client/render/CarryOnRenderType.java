// package tschipp.carryon.client.render;

// import com.mojang.blaze3d.pipeline.BlendFunction;
// import com.mojang.blaze3d.pipeline.RenderPipeline;
// import com.mojang.blaze3d.platform.DepthTestFunction;
// import com.mojang.blaze3d.vertex.MeshData;
// import com.mojang.blaze3d.vertex.VertexFormat;
// import net.minecraft.client.renderer.RenderType;
// import org.jetbrains.annotations.NotNull;

// import java.util.IdentityHashMap;
// import java.util.Map;

// //Credit: klikli for the idea and code for this wrapper
// public class CarryOnRenderType extends RenderType {

//    private static final Map<RenderType, RenderType> remappedTypes = new IdentityHashMap<>();
//    private final RenderPipeline pipeline;
//    private final RenderType original;

//    private CarryOnRenderType(RenderType original, RenderPipeline pipeline) {
//        super(String.format("%s_carryon", original.toString()), original.bufferSize(), original.affectsCrumbling(), true, original::setupRenderState, original::clearRenderState);
//        this.pipeline = pipeline;
//        this.original = original;
//    }

//    public static RenderType remap(RenderType in) {
//        return remappedTypes.computeIfAbsent(in, (type) -> {

//            //modify the pipeline
//            var pipeline = toBuilder(in.pipeline())
//                    .withBlend(BlendFunction.TRANSLUCENT)
//                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
//                    .withCull(false);

//            return new CarryOnRenderType(type, pipeline.build());
//        });
//    }

//    private static RenderPipeline.Builder toBuilder(RenderPipeline pipeline) {
//        RenderPipeline.Builder builder = RenderPipeline.builder();
//        builder.withLocation(pipeline.getLocation());
//        builder.withFragmentShader(pipeline.getFragmentShader());
//        builder.withVertexShader(pipeline.getVertexShader());

//        if (!pipeline.getShaderDefines().isEmpty()) {
//            for (Map.Entry<String, String> entry : pipeline.getShaderDefines().values().entrySet()) {
//                try {
//                    int parsed = Integer.parseInt(entry.getValue());
//                    builder.withShaderDefine(entry.getKey(), parsed);
//                } catch (NumberFormatException e) {
//                    float parsed = Float.parseFloat(entry.getValue());
//                    builder.withShaderDefine(entry.getKey(), parsed);
//                }
//            }
//            for (String flag : pipeline.getShaderDefines().flags()) {
//                builder.withShaderDefine(flag);
//            }
//        }

//        if (!pipeline.getSamplers().isEmpty()) {
//            pipeline.getSamplers().forEach(builder::withSampler);
//        }

//        if (!pipeline.getUniforms().isEmpty()) {
//            pipeline.getUniforms().forEach(u -> builder.withUniform(u.name(), u.type()));
//        }

//        builder.withDepthTestFunction(pipeline.getDepthTestFunction());
//        builder.withPolygonMode(pipeline.getPolygonMode());
//        builder.withCull(pipeline.isCull());
//        builder.withColorWrite(pipeline.isWriteColor(), pipeline.isWriteAlpha());
//        builder.withDepthWrite(pipeline.isWriteDepth());
//        builder.withColorLogic(pipeline.getColorLogic());

//        if (pipeline.getBlendFunction().isPresent())
//            builder.withBlend(pipeline.getBlendFunction().get());
//        else
//            builder.withoutBlend();
//        builder.withVertexFormat(pipeline.getVertexFormat(), pipeline.getVertexFormatMode());
//        builder.withDepthBias(pipeline.getDepthBiasScaleFactor(), pipeline.getDepthBiasConstant());

//        return builder;
//    }

//    @Override
//    public void draw(@NotNull MeshData meshData) {
//        this.original.draw(meshData);
//    }




//    @Override
//    public @NotNull VertexFormat format() {
//        return this.original.format();
//    }

//    @Override
//    public VertexFormat.@NotNull Mode mode() {
//        return this.original.mode();
//    }

//    @Override
//    public RenderPipeline pipeline() {
//        return this.pipeline; //get our own modified pipeline
//    }
// }
