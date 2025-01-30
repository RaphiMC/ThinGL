#version 330 core

uniform sampler2D u_Source;
uniform sampler2D u_Mask;
uniform bool u_FinalPass;
uniform int u_Width;

in vec2 v_VpPixelSize;
in vec2 v_VpTexCoords;
out vec4 o_Color;

void main() {
    vec4 currentPixel = texture(u_Mask, v_VpTexCoords);
    if (currentPixel.a == 0.0) {
        for (int i = -u_Width; i <= u_Width; i++) {
            vec2 coords = v_VpTexCoords + (u_FinalPass ? vec2(0, i) : vec2(i, 0)) * v_VpPixelSize;
            vec4 maskPixel;
            if(u_FinalPass) {
                maskPixel = texture(u_Source, coords);
            } else {
                maskPixel = texture(u_Mask, coords);
            }
            if (maskPixel.a != 0.0) {
                o_Color = vec4(maskPixel.rgb, 1.0);
                return;
            }
        }
    } else if (!u_FinalPass) {
        o_Color = currentPixel;
        return;
    }

    discard;
}
