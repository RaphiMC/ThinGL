#version 430 core
#define M_PI 3.14159265359

struct StringData {
    uint textColor;
    uint outlineColor;
    uint styleFlags;
    float smoothing;
};
struct CharData {
    uint textureAndStringIndex;
};

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;
uniform vec2 u_Viewport;

layout (std430) restrict readonly buffer ssbo_StringData {
    StringData stringDatas[];
};
layout (std430) restrict readonly buffer ssbo_CharData {
    CharData charDatas[];
};

layout (location = 0) in vec3 i_Position;
layout (location = 1) in vec2 i_TexCoords;
out vec2 v_TexCoords;
flat out uint v_TextureIndex;
flat out vec4 v_TextColor;
flat out vec4 v_OutlineColor;
flat out float v_Smoothing;
flat out uint v_StyleFlags;
out float v_PerspectiveScale;

void main() {
    vec4 worldPosition = u_ViewMatrix * u_ModelMatrix * vec4(i_Position, 1.0);
    gl_Position = u_ProjectionMatrix * worldPosition;

    CharData charData = charDatas[gl_VertexID / 4];
    StringData stringData = stringDatas[charData.textureAndStringIndex & 0x7FFFFFF];

    v_TexCoords = i_TexCoords;
    v_TextureIndex = (charData.textureAndStringIndex >> 27) & 31;
    v_TextColor = unpackUnorm4x8(stringData.textColor);
    v_OutlineColor = unpackUnorm4x8(stringData.outlineColor);
    v_Smoothing = stringData.smoothing;
    v_StyleFlags = stringData.styleFlags;

    float distanceToCamera = length(worldPosition.xyz);
    float fov = 2.0 * atan(1.0 / u_ProjectionMatrix[1][1]) * 180.0 / M_PI;
    float viewHeight = 2.0 * distanceToCamera * tan(radians(fov * 0.5));
    v_PerspectiveScale = viewHeight / u_Viewport.y;
}
