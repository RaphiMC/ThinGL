#version 400 core
#define STYLE_BOLD_BIT 2

const float regularThreshold = 128.0 / 255.0;
const float regularBoldThreshold = 64.0 / 255.0;
const float outlineThreshold = 32.0 / 255.0;
const float outlineBoldThreshold = 96.0 / 255.0;

uniform vec4 u_ColorModifier;
uniform sampler2D u_Textures[32];

in vec2 v_TexCoords;
flat in int v_TextureIndex;
flat in vec4 v_TextColor;
flat in vec4 v_OutlineColor;
flat in float v_Smoothing;
flat in int v_StyleFlags;
in float v_PerspectiveScale;
out vec4 o_Color;

void main() {
    float smoothing = v_Smoothing;
    if (gl_FragCoord.w != 1.0) { /* If not 2D */
        smoothing *= v_PerspectiveScale;
    }

    float threshold = regularThreshold;
    if ((v_StyleFlags & STYLE_BOLD_BIT) != 0) {
        if (v_OutlineColor.a != 0) {
            threshold = outlineBoldThreshold;
        } else {
            threshold = regularBoldThreshold;
        }
    }

    float dist = texture(u_Textures[v_TextureIndex], v_TexCoords).r;
    float alpha = smoothstep(max(0.0, threshold - smoothing), min(1.0, threshold + smoothing), dist);
    o_Color = vec4(v_TextColor.rgb, v_TextColor.a * alpha);

    if (v_OutlineColor.a != 0) {
        o_Color = mix(v_OutlineColor, v_TextColor, alpha);
        float outlineAlpha = smoothstep(max(0.0, outlineThreshold - smoothing), min(1.0, outlineThreshold + smoothing), dist);
        o_Color = vec4(o_Color.rgb, o_Color.a * outlineAlpha);
    }

    o_Color *= u_ColorModifier;
    if (o_Color.a == 0.0) {
        discard;
    }
}
