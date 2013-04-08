/***********************************************************************
Vertex shader for normal rendering of ships.

Procedure:

1.Rotate vertex
2.Translate vertex
3.Calculate final position based on camera matrix
4.Normalize light direction
5.Rotate normals
6.Calculate light level
************************************************************************/

#version 120

uniform mat4 transformationValues;
uniform vec3 lightDir;

varying vec4 color;

void main() {
	/**************************************************
	Vertex operations starts here
	***************************************************/
	//Rotate the vertex in modelspace
	vec3 rotated = mat3(transformationValues)*vec3(gl_Vertex);
	
	//Translate the vertex in modelspace
	vec4 translated = vec4(rotated + vec3(transformationValues[3]),1.0);
	
	//Use the ModelViewProjectionMatrix to calculate world space position
	gl_Position = gl_ModelViewProjectionMatrix*translated;
	
	/*************************************************
	Light starts here
	**************************************************/
	//Normalize light direction
	vec3 lightDirNorm = normalize(lightDir);
	
	//Rotate normal
	vec3 rotNormal = mat3(transformationValues)*vec3(normalize(gl_Normal));
	
	//Calculate and set color based on light
	float light = min(1.0, (max(0.1, dot(rotNormal, lightDirNorm))));
	color = vec4(1.0f * ((light) ), 1.0f * ((light) ), 1.0f * ((light ) ), 1.0);
	
	/****************************************************
	Texture coordinates starts here
	*****************************************************/
	gl_TexCoord[0] = gl_MultiTexCoord0;
}
