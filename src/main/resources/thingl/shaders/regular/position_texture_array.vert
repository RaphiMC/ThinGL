#version 330 core

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;

layout (location = 0) in vec3 i_Position;
layout (location = 1) in vec2 i_TexCoord;
layout (location = 2) in uint i_Layer;
out vec2 v_TexCoord;
flat out uint v_Layer;

void main() {
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(i_Position, 1);

    v_TexCoord = i_TexCoord;
    v_Layer = i_Layer;
}
