namespace FelidadaeAudio {
	class ISynth {
	public:
		virtual void activateNote  	(unsigned positionX, unsigned positionY);
		virtual void deactivateNote	(unsigned positionX, unsigned positionY, float bendingIndexX, float bendingIndexY);
		
		virtual void setNoteParam	(unsigned positionX, unsigned positionY);
		virtual void resetNoteParam (unsigned positionX, unsigned positionY);

		virtual void unbendNote  	(unsigned positionX, unsigned positionY);
		virtual void unbendNote  	(unsigned positionX, unsigned positionY);
		virtual void releaseNote 	(unsigned positionX, unsigned positionY);
	};
}