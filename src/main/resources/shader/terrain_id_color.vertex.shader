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
//	if (gl_Vertex[1] < 0) {
//		gl_FrontColor = vec4(1.0, 0.75, 0.5, 1.0);
//	}
	
	//Forward texture coordinates for the main texture
	gl_TexCoord[0] = gl_MultiTexCoord0 / 4;
	gl_TexCoord[0].p = 0.875;
	if (gl_Normal[1] < 0.80) {
		gl_TexCoord[0].p = 0.625;
	}
	if (gl_Vertex[1] < 5 ) {
		gl_TexCoord[0].p = 0.125;
	}
	if (gl_Vertex[1] > 5000 || gl_Normal[1] < 0.75) {
		gl_TexCoord[0].p = 0.375;
	}
}
	