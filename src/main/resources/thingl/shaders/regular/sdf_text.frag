#version 400 core
#include "../util/math.glsl"

uniform vec4 u_ColorModifier;
uniform sampler2D u_Textures[16];

in vec2 v_TexCoord;
flat in uint v_TextureIndex;
flat in uint v_FontSize;
flat in vec4 v_TextColor;
flat in vec4 v_OutlineColor;
flat in uint v_StyleFlags;
out vec4 o_Color;

void main() {
    vec3 msd = texture(u_Textures[v_TextureIndex], v_TexCoord).rgb;
    float dist = median(msd.r, msd.g, msd.b);
    if ((v_StyleFlags & uint(STYLE_BOLD_BIT)) == 0 && v_OutlineColor.a == 0.0) { // High quality text rendering
        vec2 unitRange = vec2(float(DF_PX_RANGE)) / vec2(textureSize(u_Textures[v_TextureIndex], 0));
        vec2 screenTexSize = vec2(1.0) / fwidth(v_TexCoord);
        float screenPxRange = max(dot(unitRange, screenTexSize), 1.0);
        float screenPxDistance = screenPxRange * (dist - 0.5);
        float alpha = clamp(screenPxDistance + 0.5, 0.0, 1.0);
        o_Color = vec4(v_TextColor.rgb, v_TextColor.a * alpha);
    } else { // Regular text rendering
        float width = fwidth(dist);
        float center = 0.5;
        if ((v_StyleFlags & uint(STYLE_BOLD_BIT)) != 0) {
            center = clamp(center - float(v_FontSize) / 64.0 / 10.0, 0.05, 0.5);
        }
        float alpha = smoothstep(max(center - width, 0.0), min(center + width, 1.0), dist);
        o_Color = vec4(v_TextColor.rgb, v_TextColor.a * alpha);

        if (v_OutlineColor.a != 0) {
            o_Color = mix(v_OutlineColor, v_TextColor, alpha);
            float outlineCenter = clamp(center - float(v_FontSize) / 32.0 / 10.0, 0.05, 0.5);
            float outlineAlpha = smoothstep(max(outlineCenter - width, 0.0), min(outlineCenter + width, 1.0), dist);
            o_Color = vec4(o_Color.rgb, o_Color.a * outlineAlpha);
        }
    }

    o_Color *= u_ColorModifier;
    if (o_Color.a == 0.0) {
        discard;
    }
}
