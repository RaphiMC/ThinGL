#version 400 core
#include "../util/math.glsl"

uniform vec4 u_ColorModifier;
uniform sampler2D u_Textures[16];

in vec2 v_TexCoord;
flat in uint v_TextureIndex;
flat in vec2 v_DF_Range;
flat in vec4 v_TextColor;
flat in vec4 v_OutlineColor;
flat in uint v_StyleFlags;
flat in float v_BoldnessExpansion;
out vec4 o_Color;

float computeAlpha(float screenPxRange, float dist);

void main() {
    vec2 screenTexSize = vec2(1.0) / fwidth(v_TexCoord);
    float screenPxRange = max(dot(v_DF_Range, screenTexSize), 1.0);

    vec3 msd = texture(u_Textures[v_TextureIndex], v_TexCoord).rgb;
    float dist = median(msd.r, msd.g, msd.b);
    if (bool(v_StyleFlags & STYLE_BOLD_BIT)) {
        dist += clamp(v_BoldnessExpansion, 0.0, 0.22);
    }
    o_Color = v_TextColor;
    o_Color.a *= computeAlpha(screenPxRange, dist);

    if (v_OutlineColor.a != 0.0) {
        dist += clamp(v_BoldnessExpansion, 0.0, 0.22);
        float outlineAlpha = computeAlpha(screenPxRange, dist);
        o_Color = mix(v_OutlineColor, vec4(o_Color.rgb, 1.0), o_Color.a);
        o_Color.a *= outlineAlpha;
    }

    o_Color *= u_ColorModifier;
    if (o_Color.a == 0.0) {
        discard;
    }
}

float computeAlpha(float screenPxRange, float dist) {
    float screenPxDistance = screenPxRange * (dist - 0.5);
    return clamp(screenPxDistance + 0.5, 0.0, 1.0);
}
