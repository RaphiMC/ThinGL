#version 330 core

uniform vec4 u_ColorModifier;
uniform sampler2DArray u_Texture;

in vec2 v_TexCoord;
flat in uint v_Layer;
out vec4 o_Color;

void main() {
    o_Color = texture(u_Texture, vec3(v_TexCoord, v_Layer)) * u_ColorModifier;
    if (o_Color.a == 0) {
        discard;
    }
}
