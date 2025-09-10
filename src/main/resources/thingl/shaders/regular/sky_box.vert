#version 330 core

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;

layout (location = 0) in vec3 a_Position;
out vec3 v_TexCoord;

void main() {
    gl_Position = u_ProjectionMatrix * mat4(mat3(u_ViewMatrix)) * mat4(mat3(u_ModelMatrix)) * vec4(a_Position, 1.0);
    gl_Position.z = gl_Position.w;

    v_TexCoord = a_Position * vec3(1, 1, -1);
}
