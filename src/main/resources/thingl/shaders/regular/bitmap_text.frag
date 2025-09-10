#version 400 core

uniform vec4 u_ColorModifier;
uniform sampler2D u_Textures[32];
uniform bool u_EdgeSharpening;

in vec2 v_TexCoord;
flat in uint v_TextureIndex;
flat in vec4 v_TextColor;
out vec4 o_Color;

void main() {
    vec4 textureColor = texture(u_Textures[v_TextureIndex], v_TexCoord);
    if (u_EdgeSharpening) {
        float alphaWidth = fwidth(textureColor.a);
        textureColor.a = smoothstep(0.5 - alphaWidth, 0.5 + alphaWidth, textureColor.a);
    }
    o_Color = textureColor * v_TextColor * u_ColorModifier;
    if (o_Color.a == 0) {
        discard;
    }
}
