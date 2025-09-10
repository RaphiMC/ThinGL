#version 400 core
#define DF_PX_RANGE 6
#define STYLE_BOLD_BIT 2u

uniform vec4 u_ColorModifier;
uniform sampler2D u_Textures[32];

in vec2 v_TexCoord;
flat in uint v_TextureIndex;
flat in uint v_FontSize;
flat in vec4 v_TextColor;
flat in vec4 v_OutlineColor;
flat in uint v_StyleFlags;
out vec4 o_Color;

float median(float r, float g, float b);

void main() {
    vec3 msd = texture(u_Textures[v_TextureIndex], v_TexCoord).rgb;
    float dist = median(msd.r, msd.g, msd.b);
    if ((v_StyleFlags & STYLE_BOLD_BIT) == 0 && v_OutlineColor.a == 0) { // High quality text rendering
        vec2 unitRange = vec2(DF_PX_RANGE) / vec2(textureSize(u_Textures[v_TextureIndex], 0));
        vec2 screenTexSize = vec2(1) / fwidth(v_TexCoord);
        float screenPxRange = max(dot(unitRange, screenTexSize), 1);
        float screenPxDistance = screenPxRange * (dist - 0.5);
        float alpha = clamp(screenPxDistance + 0.5, 0, 1);
        o_Color = vec4(v_TextColor.rgb, v_TextColor.a * alpha);
    } else { // Regular text rendering
        float width = fwidth(dist);
        float center = 0.5;
        if ((v_StyleFlags & STYLE_BOLD_BIT) != 0) {
            center = clamp(center - v_FontSize / 64.0 / 10.0, 0.05, 0.5);
        }
        float alpha = smoothstep(max(center - width, 0), min(center + width, 1), dist);
        o_Color = vec4(v_TextColor.rgb, v_TextColor.a * alpha);

        if (v_OutlineColor.a != 0) {
            o_Color = mix(v_OutlineColor, v_TextColor, alpha);
            float outlineCenter = clamp(center - v_FontSize / 32.0 / 10.0, 0.05, 0.5);
            float outlineAlpha = smoothstep(max(outlineCenter - width, 0), min(outlineCenter + width, 1), dist);
            o_Color = vec4(o_Color.rgb, o_Color.a * outlineAlpha);
        }
    }

    o_Color *= u_ColorModifier;
    if (o_Color.a == 0) {
        discard;
    }
}

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}
