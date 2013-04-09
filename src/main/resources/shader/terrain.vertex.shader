/***********************************************************************
Vertex shader for rendering for mouse picking.

Procedure:

1.Calculate final position based on camera matrix
2.Set color to red (for testing)
************************************************************************/

#version 120

void main() {
	/**************************************************
	Vertex operations starts here
	***************************************************/
	//Use the ModelViewProjectionMatrix to calculate world space position
	gl_Position = gl_ModelViewProjectionMatrix*gl_Vertex;
	
	//Set the color	
	gl_FrontColor = vec4(0.5, 0.5, 0.5, 1.0);
}
	