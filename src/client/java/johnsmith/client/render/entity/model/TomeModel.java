package johnsmith.client.render.entity.model;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

/**
 * Custom book model that includes a buckle.
 * Based on the 1.20.6 BookModel.
 */
public class TomeModel extends BookModel {

    // Store the root part
    private final ModelPart root;

    // New custom part
    private final ModelPart buckle;

    public TomeModel(ModelPart root) {
        super(root);
        this.root = root;
        this.buckle = root.getChild("right_lid").getChild("buckle");
    }

    /**
     * Defines the geometry for the custom book model.
     * This uses the 1.20.6 vanilla BookModel part names,
     * with the "buckle" part attached to the "right_lid".
     */
    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData root = modelData.getRoot();
        root.addChild("left_lid", ModelPartBuilder.create().uv(0, 0).cuboid(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), ModelTransform.pivot(0.0F, 0.0F, -1.0F));
        // Get the reference for later use
        ModelPartData rightLid = root.addChild("right_lid", ModelPartBuilder.create().uv(16, 0).cuboid(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F), ModelTransform.pivot(0.0F, 0.0F, 1.0F));
        root.addChild("seam", ModelPartBuilder.create().uv(12, 0).cuboid(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F), ModelTransform.rotation(0.0F, ((float)Math.PI / 2F), 0.0F));
        root.addChild("left_pages", ModelPartBuilder.create().uv(0, 10).cuboid(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), ModelTransform.NONE);
        root.addChild("right_pages", ModelPartBuilder.create().uv(12, 10).cuboid(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), ModelTransform.NONE);
        ModelPartBuilder modelPartBuilder = ModelPartBuilder.create().uv(24, 10).cuboid(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
        root.addChild("flip_page1", modelPartBuilder, ModelTransform.NONE);
        root.addChild("flip_page2", modelPartBuilder, ModelTransform.NONE);

        // Create the buckle
        ModelPartBuilder buckleBuilder = ModelPartBuilder.create()
                .uv(28, 4) // Texture UV coordinates
                .cuboid(
                        0.0F, -1.0F, 0.0F, // X, Y, Z (relative to parent, "right_lid")
                        2.0F, 2.0F, 0.0F,    // Width, Height, Depth
                        new Dilation(0.0F)
                );

        // Add the buckle as a child of rightLid
        // We apply a 90-degree (PI / 2) rotation around the Y-axis.
        rightLid.addChild("buckle", buckleBuilder,
                ModelTransform.of(
                        6.0F, 0.0F, 0.0F,  // Pivot Point (X=6 is edge of cover, Y=0 is center)
                        0.0F, (float)(Math.PI / 2F), 0.0F   // Initial Rotation (X, Y, Z)
                )
        );

        return TexturedModelData.of(modelData, 64, 32);
    }

    /**
     * Overrides the vanilla render method to call root.render()
     */
    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        // Just render the root, which now includes the buckle
        this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    /**
     * Overrides the page angle logic to also move the buckle
     * along with the right cover.
     */
    @Override
    public void setPageAngles(float pageTurnAmount, float leftFlipAmount, float rightFlipAmount, float pageTurnSpeed) {
        // Call the super method to animate all the vanilla parts
        // (including the right_lid, the buckle's parent)
        super.setPageAngles(pageTurnAmount, leftFlipAmount, rightFlipAmount, pageTurnSpeed);
        this.buckle.yaw = (float)(Math.PI / 2F) - (pageTurnSpeed * (float)Math.PI);
    }
}