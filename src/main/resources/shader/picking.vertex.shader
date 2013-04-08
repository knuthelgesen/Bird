/***********************************************************************
Vertex shader for rendering for mouse picking.

Procedure:

1.Rotate vertex
2.Translate vertex
3.Calculate final position based on camera matrix
4.Set color to the provided value (which is the ID of the mesh)
************************************************************************/

#version 120

uniform mat4 transformationValues;

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
		
	gl_FrontColor = gl_Color;
}
