#version 430 core

struct TextData {
    uint textColor;
    uint outlineColor;
    uint styleFlags;
    float boldnessExpansion;
};

uniform mat4 u_ProjectionMatrix;
uniform mat4 u_ViewMatrix;
uniform mat4 u_ModelMatrix;
uniform sampler2D u_Textures[16];

layout (std430) restrict readonly buffer ssbo_TextData {
    TextData textDatas[];
};

layout (location = 0) in vec3 a_Position;
layout (location = 1) in vec2 a_TexCoord;
layout (location = 2) in uint a_TextureIndex;
layout (location = 3) in uint a_TextDataIndex;
out vec2 v_TexCoord;
flat out uint v_TextureIndex;
flat out vec2 v_DF_Range;
flat out vec4 v_TextColor;
flat out vec4 v_OutlineColor;
flat out uint v_StyleFlags;
flat out float v_BoldnessExpansion;

void main() {
    gl_Position = u_ProjectionMatrix * u_ViewMatrix * u_ModelMatrix * vec4(a_Position, 1.0);

    v_TexCoord = a_TexCoord;
    v_TextureIndex = a_TextureIndex;
    v_DF_Range = vec2(float(DF_PX_RANGE)) / vec2(textureSize(u_Textures[v_TextureIndex], 0));

    TextData textData = textDatas[a_TextDataIndex];
    v_TextColor = unpackUnorm4x8(textData.textColor);
    v_OutlineColor = unpackUnorm4x8(textData.outlineColor);
    v_StyleFlags = textData.styleFlags;
    v_BoldnessExpansion = textData.boldnessExpansion / float(DF_PX_RANGE) / 2.0;
}
