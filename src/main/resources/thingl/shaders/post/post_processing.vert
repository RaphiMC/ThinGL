#version 330 core

uniform mat4 u_ProjectionMatrix;
uniform vec2 u_Viewport;
uniform vec4 u_Rectangle;

out vec2 v_RelPixelSize;
out vec4 v_RelCoord;
out vec2 v_RelTexCoord;
out vec2 v_VpPixelSize;
out vec4 v_VpCoord;
out vec2 v_VpTexCoord;

vec2 rectangleCoords[6] = vec2[](u_Rectangle.xy, u_Rectangle.xw, u_Rectangle.zy, u_Rectangle.zy, u_Rectangle.xw, u_Rectangle.zw);

void main() {
    vec3 a_Position = vec3(rectangleCoords[gl_VertexID], 0);
    gl_Position = u_ProjectionMatrix * vec4(a_Position, 1);

    vec2 rectangleSize = u_Rectangle.zw - u_Rectangle.xy;

    vec2 relative = a_Position.xy - u_Rectangle.xy;
    vec4 relativeCoord = u_ProjectionMatrix * vec4(relative / rectangleSize * u_Viewport, a_Position.z, 1);

    v_RelPixelSize = 1 / rectangleSize;
    v_RelCoord = vec4(relative.x, rectangleSize.y - relative.y, a_Position.z, 1);
    v_RelTexCoord = (relativeCoord.xy + 1) / 2;

    v_VpPixelSize = 1 / u_Viewport;
    v_VpCoord = vec4(a_Position.x, u_Viewport.y - a_Position.y, a_Position.z, 1);
    v_VpTexCoord = (gl_Position.xy + 1) / 2;
}
