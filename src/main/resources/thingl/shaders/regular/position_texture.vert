#version 330 core

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;

layout (location = 0) in vec3 i_Position;
layout (location = 1) in vec2 i_TexCoords;
out vec2 v_TexCoords;

void main() {
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(i_Position, 1);

    v_TexCoords = i_TexCoords;
}
