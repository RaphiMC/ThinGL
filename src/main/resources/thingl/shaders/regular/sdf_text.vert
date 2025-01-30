#version 430 core
#define M_PI 3.14159265359

struct StringData {
    int textColor;
    int outlineColor;
    int styleFlags;
    float smoothing;
};
struct CharData {
    int textureAndStringIndex;
};

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;

layout (std430) readonly buffer ssbo_StringData {
    StringData stringDatas[];
};
layout (std430) readonly buffer ssbo_CharData {
    CharData charDatas[];
};

layout (location = 0) in vec3 i_Position;
layout (location = 1) in vec2 i_TexCoords;
out vec2 v_TexCoords;
flat out int v_TextureIndex;
out vec4 v_TextColor;
out vec4 v_OutlineColor;
out float v_Smoothing;
flat out int v_StyleFlags;
out float v_DistanceToCamera;
out float v_Fov;

vec4 decodeColor(int rgba);

void main() {
    vec4 worldPosition = u_ViewMatrix * u_ModelMatrix * vec4(i_Position, 1.0);
    gl_Position = u_ProjectionMatrix * worldPosition;

    CharData charData = charDatas[gl_VertexID / 4];
    StringData stringData = stringDatas[charData.textureAndStringIndex & 0x7FFFFFF];

    v_TexCoords = i_TexCoords;
    v_TextureIndex = (charData.textureAndStringIndex >> 27) & 31;
    v_TextColor = decodeColor(stringData.textColor);
    v_OutlineColor = decodeColor(stringData.outlineColor);
    v_Smoothing = stringData.smoothing;
    v_StyleFlags = stringData.styleFlags;
    v_DistanceToCamera = length(worldPosition.xyz);
    v_Fov = 2.0 * atan(1.0 / u_ProjectionMatrix[1][1]) * 180.0 / M_PI;
}

vec4 decodeColor(int rgba) {
    return vec4(
        ((rgba >> 24) & 0xFF) / 255.0,
        ((rgba >> 16) & 0xFF) / 255.0,
        ((rgba >> 8) & 0xFF) / 255.0,
        (rgba & 0xFF) / 255.0
    );
}
