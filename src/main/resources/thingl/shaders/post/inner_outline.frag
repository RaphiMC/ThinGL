#version 330 core

uniform sampler2D u_Mask;
uniform int u_Width;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoords;
out vec4 o_Color;

void main() {
    vec4 currentPixel = texture(u_Mask, v_VpTexCoords);
    if (currentPixel.a != 0.0) {
        for (int x = -u_Width; x <= u_Width; x++) {
            for (int y = -u_Width; y <= u_Width; y++) {
                vec4 maskPixel = texture(u_Mask, v_VpTexCoords + vec2(x, y) * v_VpPixelSize);
                if (maskPixel.a == 0.0) {
                    o_Color = vec4(currentPixel.rgb, 1.0);
                    return;
                }
            }
        }
    }

    discard;
}
