
#ifndef Earing_Tuner_h
#define Earing_Tuner_h

#include "ITuner.h"

#include <vector>



class Tuner: public ITuner {
public:
	Tuner(unsigned secondsStepBetweenRowsFromBas, unsigned numberOfStrings) {
		for(unsigned i=0; i< numberOfStrings; ++i)
			secondsBetweenRowsFromBas_.push_back(secondsStepBetweenRowsFromBas);
	}
	Tuner(unsigned baseFrequency, std::vector<int> secondsBetweenRowsFromBas) {
		setTuning(baseFrequency, secondsBetweenRowsFromBas_);
	}
	
	unsigned getFrequencyFromPosition(unsigned positionX, unsigned positionY) {
		/* Note: position (row,string==0,column,fret==0) is the lowest frequency note*/
		if(positionY > secondsBetweenRowsFromBas_.size())
			return 0; //error
		unsigned frequencyIndex = 0;
		for(unsigned i=0; i < positionY; ++i)
			frequencyIndex += secondsBetweenRowsFromBas_[i];
		return (frequencyIndex+positionX);
	}
	void setTuning(unsigned baseFrequency, const std::vector<int> secondsBetweenRowsFromBas) {
		baseFrequency_ = baseFrequency;
		secondsBetweenRowsFromBas_ = secondsBetweenRowsFromBas;
	}
	
private:
	unsigned baseFrequency_;
	std::vector<int> secondsBetweenRowsFromBas_;
};


class SimpleTuner: public ITuner {
public:
	SimpleTuner(unsigned secondsStepBetweenRowsFromBas) {
		this->secondsStepBetweenRowsFromBas_ = secondsStepBetweenRowsFromBas;
	}
	unsigned getFrequencyFromPosition(unsigned positionX, unsigned positionY) {
		return positionY*secondsStepBetweenRowsFromBas_ + positionX;
	}

private:
	unsigned secondsStepBetweenRowsFromBas_;	
};

#endif
