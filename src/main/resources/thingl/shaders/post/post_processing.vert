#version 330 core

uniform mat4 u_ProjectionMatrix;
uniform vec2 u_Viewport;
uniform vec4 u_Quad;

out vec2 v_RelPixelSize;
out vec4 v_RelCoords;
out vec2 v_RelTexCoords;
out vec2 v_VpPixelSize;
out vec4 v_VpCoords;
out vec2 v_VpTexCoords;

vec2 quadValues[6] = vec2[](u_Quad.xy, u_Quad.xw, u_Quad.zy, u_Quad.zy, u_Quad.xw, u_Quad.zw);

void main() {
    vec3 i_Position = vec3(quadValues[gl_VertexID], 0);
    gl_Position = u_ProjectionMatrix * vec4(i_Position, 1);

    vec2 resolution = u_Quad.zw - u_Quad.xy;

    vec2 relative = i_Position.xy - u_Quad.xy;
    vec4 relativeCoords = u_ProjectionMatrix * vec4(relative / resolution * u_Viewport, i_Position.z, 1);

    v_RelPixelSize = 1 / resolution;
    v_RelCoords = vec4(relative.x, resolution.y - relative.y, i_Position.z, 1);
    v_RelTexCoords = (relativeCoords.xy + 1) / 2;

    v_VpPixelSize = 1 / u_Viewport;
    v_VpCoords = vec4(i_Position.x, u_Viewport.y - i_Position.y, i_Position.z, 1);
    v_VpTexCoords = (gl_Position.xy + 1) / 2;
}
