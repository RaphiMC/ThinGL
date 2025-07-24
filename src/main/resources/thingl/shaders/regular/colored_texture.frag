#version 330 core

uniform vec4 u_ColorModifier;
uniform sampler2D u_Texture;

in vec4 v_Color;
in vec2 v_TexCoord;
out vec4 o_Color;

void main() {
    o_Color = texture(u_Texture, v_TexCoord) * v_Color * u_ColorModifier;
    if (o_Color.a == 0) {
        discard;
    }
}
