package org.ecocean.identity.BenWhitesharkPkg;

public class RunMatchContourToPoints {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[] regionContourx, regionContoury, finDetectionx, finDetectiony;
		double tipx = 0.0, tipy = 0.0, tex = 0.0, tey = 0.0, lex = 0.0, ley = 0.0;
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Initialise

		// do: load image

		// construct java object and import json
		MatchContourToPoints2 m = new MatchContourToPoints2();
		m.importJson("F:\\Dropbox\\whiteShark\\sharkIDCode4\\extern\\jsonlab-1.5\\jsonFile2.json");
		

		// get image size for which contours match up
		int szy = m.getSz("y");
		int szx = m.getSz("x");

		// do: resize image to dimensions szy and szx

		regionContourx = m.getSelectedRegionContour("x");
		regionContoury = m.getSelectedRegionContour("y");
		// do: draw initial region contour on image in colour1

		finDetectionx = m.getFinDetectionContour("x");
		finDetectiony = m.getFinDetectionContour("y");
		// do: draw initial fin detection contour on image in colour2

		// if no previously labelled / stored keypoints, returns -1
		// otherwise get stored keypoint coordinates
		if (m.getTipPt("x") != -1) {
			tipx = m.getTipPt("x");
			tipy = m.getTipPt("y");
			lex = m.getLePt("x");
			ley = m.getLePt("y");
			tex = m.getTePt("x");
			tey = m.getTePt("y");
			// do: draw initial points on image
		}

		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//With callbacks etc.....
		
		// user labels (or drags?) points on image.
		// creates or updates positions of keypoints (tip = tip; le = start of leading edge; te = end of trailing edge)
		m.setLePt(lex, ley);
		m.setTePt(tex, tey);
		m.setTipPt(tipx, tipy);

		// matches the new keypoint locations to region contours
		m.matchPointsToContours();

		// do: clear existing region contour
		// do: clear existing fin detection contour

		regionContourx = m.getSelectedRegionContour("x");
		regionContoury = m.getSelectedRegionContour("y");
		// do: draw new region contour on image in colour1

		finDetectionx = m.getFinDetectionContour("x");
		finDetectiony = m.getFinDetectionContour("y");
		// do: draw new region contour on image in colour2

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		//If after edits, a satisfactory detection is not possible, use
		m.setDetectionIsBad(1);
	

		// Once user finished / wants to save edits.....
		m.exportJson();

		// do: update (new) dateDetectionModified field in WB (?) + edit WB detectionIsBadField if necessary (?)

	}

}
