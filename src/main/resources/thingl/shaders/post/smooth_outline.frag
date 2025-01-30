#version 330 core

uniform sampler2D u_Mask;
uniform int u_Width;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoords;
out vec4 o_Color;

void main() {
    if (texture(u_Mask, v_VpTexCoords).a == 0.0) {
        int maxDist = u_Width * u_Width;

        for (int x = -u_Width; x <= u_Width; x++) {
            for (int y = -u_Width; y <= u_Width; y++) {
                vec4 maskPixel = texture(u_Mask, v_VpTexCoords + vec2(x, y) * v_VpPixelSize);
                if (maskPixel.a != 0.0) {
                    int dist = x * x + y * y;
                    if (dist <= maxDist) {
                        o_Color = vec4(maskPixel.rgb, 1.0);
                        return;
                    }
                }
            }
        }
    }

    discard;
}
