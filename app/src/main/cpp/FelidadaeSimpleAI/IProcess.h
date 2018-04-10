#ifndef FilterModule_Standalone_IProcess_h
#define FilterModule_Standalone_IProcess_h

#include "AudioInOutBuffers.h"


namespace FelidadaeAudio {
	template<class SampleType>
	class IProcess {
	public:
		virtual ~IProcess() {}
		virtual void process (AudioInOutBuffers<SampleType>& audioBlocks) = 0;
		virtual void reset() {};
	};
}


#endif
