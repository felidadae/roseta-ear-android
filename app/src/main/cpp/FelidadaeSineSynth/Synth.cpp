#include "Synth.h"
#include <cmath>





#pragma mark __class Synth::PlayingNoteInfo

Synth::PlayingNoteInfo::PlayingNoteInfo() {
	reset();
}

float_type Synth::PlayingNoteInfo::getSampleValue() {
	return amplitude * sin( phi / Synth::sampleRate );
}

void Synth::PlayingNoteInfo::updateState() {
	//Time
	++t_p;
	if(ifDp)
		++t_dp;

	//Frequency
	frequency = ( pow(2.0, (idx+12.0+bendingX*0.1/12.0)/12.0) * theLowestNoteFrequency );
	phi += 2.0*M_PI * frequency * 1;

	//Amplitude
	float standard_amplitude = 0.2;
	float max_amplitude = standard_amplitude;
	/* float max_amplitude = 200 / sqrt(frequency) * standard_amplitude; */
	if(!(ifDp))
	{
		if( amplitude < max_amplitude )
			amplitude += 0.00003 * max_amplitude / standard_amplitude;
		//amplitude += bendingY*0.0001;
	}
	else
	{
		if( amplitude > 0.0 ){
			if(amplitude < 0.000001)
				amplitude = 0.0;
			else
				amplitude -= 0.000004 * max_amplitude / standard_amplitude;
		}
	}
	
}

bool Synth::PlayingNoteInfo::ifNoteShouldReset() {
	if( amplitude <= 0.0 && ifDp == true) return true;
	return false;
}

bool Synth::PlayingNoteInfo::ifExistNote() {
	if( idx <= 0) return false;
	return true;
}

void Synth::PlayingNoteInfo::reset() {
	idx = 0;
	amplitude = 0.0;
	bendingX = bendingY = 0;
	frequency = 0.0;
	phi = 0;
	ifDp = false;
	t_p = 0;
	t_dp = 0;
}





#pragma mark __class Synth::Synth

Synth::Synth(ITuner* tuner) { this->tuner_ = tuner; }

void Synth::process (FelidadaeAudio::AudioInOutBuffers<float_type>& audioBlocks) {
	unsigned inNumberFrames = audioBlocks.channelLength_;
	float_type* outputBuffer = audioBlocks.out_;
	
	for (unsigned frameNum = 0; frameNum < inNumberFrames; frameNum++)
		outputBuffer[frameNum] = 0;
	
	for(unsigned int i = 0; i < maxActiveNotes; ++i)
	{
		if( !notes[i].ifExistNote() ) continue;
		
		for (unsigned frameNum = 0; frameNum < inNumberFrames; frameNum++)
		{
			notes[i].updateState();
			if( notes[i].ifNoteShouldReset() ) {
				notes[i].reset();
				break;
			}
			outputBuffer[frameNum] += notes[i].getSampleValue();
			
			/* Monochannel synth -> copy result for others channels*/
			for(unsigned channelI = 1; channelI < audioBlocks.numOfChannels_; ++ channelI)
				outputBuffer[frameNum + channelI*audioBlocks.channelLength_] = outputBuffer[frameNum];
		}
	}
}


#pragma mark __class Synth::Synth modern Events

void Synth::attackNote  (unsigned indexOfNote) {
	 for(int i = 0; i < maxActiveNotes; ++i)
		 if(notes[i].idx == 0)
		 {
			 notes[i].idx = indexOfNote;
			 notes[i].t_p = 0;
			 notes[i].amplitude = 0.0;
			 break;
		 }
}

void Synth::releaseNote(unsigned indexOfnote) {
	for(int i = 0; i < maxActiveNotes; ++i)
		if( indexOfnote == notes[i].idx )
		{
			notes[i].ifDp = true;
			continue;
		}
}

void Synth::bendNote    (unsigned indexOfNote, float bendingIndexX, float bendingIndexY) {
	for(int i = 0; i < maxActiveNotes; ++i)
		if(notes[i].idx == indexOfNote)
		{
			notes[i].bendingX = bendingIndexX;
			notes[i].bendingY = bendingIndexY;
			break;
		}
}

void Synth::unbendNote  (unsigned indexOfNote) {
			
}


#pragma mark __class Synth::Synth modern Events

unsigned Synth::findNoteIndexFrequency(unsigned positionX, unsigned positionY) {
	return tuner_->getFrequencyFromPosition(positionX, positionY);
}

void Synth::attackNote  (unsigned positionX, unsigned positionY) {
	attackNote(findNoteIndexFrequency(positionX, positionY));
}

void Synth::bendNote    (unsigned positionX, unsigned positionY, float bendingIndexX, float bendingIndexY) {
	bendNote(findNoteIndexFrequency(positionX, positionY), bendingIndexX, bendingIndexY);
}

void Synth::unbendNote  (unsigned positionX, unsigned positionY) {
	unbendNote(findNoteIndexFrequency(positionX, positionY));
}

void Synth::releaseNote(unsigned positionX, unsigned positionY) {
	releaseNote(findNoteIndexFrequency(positionX, positionY));
}
