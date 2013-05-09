/***********************************************************************
Vertex shader for rendering for mouse picking.
Procedure:

1.Calculate final position based on camera matrix
2.Set color to red (for testing)
************************************************************************/

#version 120

varying vec2 pos;

void main() {
	/**************************************************
	Vertex operations starts here
	***************************************************/
	//Use the ModelViewProjectionMatrix to calculate world space position
	gl_Position = gl_ModelViewProjectionMatrix*gl_Vertex;
	
	pos = gl_Vertex.xz * 8;
	
	//Set the color	
	gl_FrontColor = vec4(gl_Normal[1], gl_Normal[1], gl_Normal[1], 1.0);
	gl_FrontColor = gl_FrontColor * gl_Color;
	
	//Forward texture coordinates for the main texture
	gl_TexCoord[0].st = gl_MultiTexCoord0.st / 4;
}
	