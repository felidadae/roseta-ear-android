#ifndef Earing_ITuner_h
#define Earing_ITuner_h



class ITuner {
public:
	virtual unsigned getFrequencyFromPosition(unsigned positionX, unsigned positionY) = 0;
};

#endif
