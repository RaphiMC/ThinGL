#version 330 core

uniform vec4 u_ColorModifier;
uniform sampler2D u_Texture;

in vec2 v_TexCoords;
out vec4 o_Color;

void main() {
    o_Color = texture(u_Texture, v_TexCoords) * u_ColorModifier;
    if (o_Color.a == 0.0) {
        discard;
    }
}
