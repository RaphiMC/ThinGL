#version 400 core

uniform vec4 u_ColorModifier;
uniform sampler2D u_Textures[32];

in vec2 v_TexCoord;
flat in uint v_TextureIndex;
flat in vec4 v_TextColor;
out vec4 o_Color;

void main() {
    float intensity = texture(u_Textures[v_TextureIndex], v_TexCoord).r;
    o_Color = vec4(v_TextColor.rgb, v_TextColor.a * intensity) * u_ColorModifier;
    if (o_Color.a == 0) {
        discard;
    }
}
