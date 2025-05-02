#version 430 core
#define M_PI 3.14159265359

struct TextData {
    uint textColor;
    uint outlineColor;
    uint styleFlags;
    float smoothing;
};
struct GlyphData {
    uint textureAndTextIndex;
};

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;
uniform vec2 u_Viewport;

layout (std430) restrict readonly buffer ssbo_TextData {
    TextData textDatas[];
};
layout (std430) restrict readonly buffer ssbo_GlyphData {
    GlyphData glyphDatas[];
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

    GlyphData glyphData = glyphDatas[gl_VertexID / 4];
    TextData textData = textDatas[glyphData.textureAndTextIndex & uint(0x7FFFFFF)];

    v_TexCoords = i_TexCoords;
    v_TextureIndex = (glyphData.textureAndTextIndex >> 27) & uint(31);
    v_TextColor = unpackUnorm4x8(textData.textColor);
    v_OutlineColor = unpackUnorm4x8(textData.outlineColor);
    v_Smoothing = textData.smoothing;
    v_StyleFlags = textData.styleFlags;

    float distanceToCamera = length(worldPosition.xyz);
    float fov = 2.0 * atan(1.0 / u_ProjectionMatrix[1][1]) * 180.0 / M_PI;
    float viewHeight = 2.0 * distanceToCamera * tan(radians(fov * 0.5));
    v_PerspectiveScale = viewHeight / u_Viewport.y;
}
