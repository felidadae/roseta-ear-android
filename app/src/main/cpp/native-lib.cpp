
//c++ --> java
#include <jni.h>

// for native audio
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>

//for asserting results of engine
#include <assert.h>

//android log
#include <android/log.h>
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "synth", __VA_ARGS__)

//math
#include <math.h>

#define PI 3.14159265

//different
#include <string>
#include <climits>

//felidadae rosetus synth
#define __IF_ANDROID__
#include "FelidadaeSineSynth/Synth.h"
#include "FelidadaeSineSynth/Tuner.h"


ITuner* tuner = new SimpleTuner(5);
Synth synth(tuner);


extern "C"
jstring
Java_com_example_felidadae_rosetus_MainActivity_stringFromJNI( JNIEnv *env, jobject /* this */) 
{
    std::string hello = "Hello from C++; Dupa pana Janka";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
void
Java_com_example_felidadae_rosetus_MainActivity_attackNote(
        JNIEnv *env,
        jobject /* this */, jint positionX, jint positionY) 
{
	synth.attackNote(positionX,positionY);
}

extern "C"
void
Java_com_example_felidadae_rosetus_MainActivity_releaseNote(
        JNIEnv *env,
        jobject /* this */, jint positionX, jint positionY) 
{
	synth.releaseNote(positionX, positionY);
}

extern "C"
void
Java_com_example_felidadae_rosetus_MainActivity_bendNote(
        JNIEnv *env,
        jobject /* this */, jint positionX, jint positionY, jfloat bendingIndexX, jfloat bendingIndexY) 
{
	synth.bendNote(positionX, positionY, bendingIndexX, bendingIndexY);
}

extern "C"
void
Java_com_example_felidadae_rosetus_MainActivity_unbendNote(
        JNIEnv *env,
        jobject /* this */, jint positionX, jint positionY) 
{
	synth.unbendNote(positionX, positionY);
}

extern "C"
void
Java_com_example_felidadae_rosetus_MainActivity_readNoteAmplitude(
        JNIEnv *env,
        jobject /* this */, jint positionX, jint positionY) 
{
	synth.readNoteAmplitude(positionX, positionY);
}

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;

// buffer queue player interfaces
static SLObjectItf bqPlayerObject = NULL;
static SLPlayItf bq_player_play;
static SLAndroidSimpleBufferQueueItf bq_player_buffer_queue;
static SLBufferQueueItf buffer_queue_itf;

//params currently static should be possible to set
static unsigned buffer_size = 2048;
static unsigned N_BUFFERS = 2;
static unsigned sample_rate = 44100;

/*
 * Creates engine, outputmix
 */
void CreateEngine() {
	SLresult result;
	result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
	assert(SL_RESULT_SUCCESS == result);

	result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
	assert(SL_RESULT_SUCCESS == result);

	result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
	assert(SL_RESULT_SUCCESS == result);

	result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, NULL, NULL);
	assert(SL_RESULT_SUCCESS == result);
	result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
	assert(SL_RESULT_SUCCESS == result);
	LOGI("engine started");
}

static long int it=0;
static int ibuf = 0;
static int16_t* buf_ptr   =new int16_t[buffer_size*2];
static int16_t* buf_newest=new int16_t[buffer_size];
static float* buf_newest_float=new float [buffer_size];
extern "C" void BqPlayerCallback(SLAndroidSimpleBufferQueueItf queueItf,
		void *data) 
{
	FelidadaeAudio::AudioInOutBuffers<float_type> audio;
	audio.channelLength_ = buffer_size;
	audio.numOfChannels_ = 1;
	audio.out_.data_ = buf_newest_float;
	synth.process(audio);

	/* cast from float to int */
	for (int i = 0; i < buffer_size; ++i, ++it) {
		/* Here sound is synthesized */
		buf_newest[i] = (int16_t) (buf_newest_float[i] * USHRT_MAX * 0.1); //casting float to int16
	}

	for (int i = 0; i < buffer_size; ++i) {
		/* Here we copy from right half to left half of buffor */
		buf_ptr[i] = buf_ptr[i+buffer_size];
	}

	for (int i = 0; i < buffer_size; ++i) {
		/* Here we copy from the newest to right half of buffor */
		buf_ptr[i+buffer_size] = buf_newest[i];
	}
	
	SLresult result = (*queueItf)->Enqueue(bq_player_buffer_queue,
		buf_ptr, buffer_size*2);
	assert(SL_RESULT_SUCCESS == result);
    //	LOGI("Buffer calculated");
	ibuf = 1;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_felidadae_rosetus_MainActivity_start(
		JNIEnv *env,
		jobject thiz, jint sample_rate, jint buf_size) 

{
	//create engine and output mix
	CreateEngine();

	SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, N_BUFFERS};
	SLDataFormat_PCM format_pcm = {
		SL_DATAFORMAT_PCM, 1, (SLuint32) 44100*1000,
		SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
		SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN
	};
	SLDataSource audio_src = {&loc_bufq, &format_pcm};

	SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
	SLDataSink audio_sink = {&loc_outmix, NULL};

	const SLInterfaceID ids[2] = {SL_IID_BUFFERQUEUE, SL_IID_VOLUME};
	const SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
	SLresult result;

	result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audio_src, &audio_sink, 1, ids, req);
	assert(SL_RESULT_SUCCESS == result);

	result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
	assert(SL_RESULT_SUCCESS == result);

	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bq_player_play);
	assert(SL_RESULT_SUCCESS == result);

	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE, &bq_player_buffer_queue);
	assert(SL_RESULT_SUCCESS == result);

	result = (*bq_player_buffer_queue)->RegisterCallback(bq_player_buffer_queue, &BqPlayerCallback, NULL);
	assert(SL_RESULT_SUCCESS == result);

	for (int i = 0; i < 1; ++i)
	 	BqPlayerCallback(bq_player_buffer_queue, NULL);

	result = (*bq_player_play)->SetPlayState(bq_player_play,
			SL_PLAYSTATE_PLAYING);
	assert(SL_RESULT_SUCCESS == result);
	LOGI("Started all succesfully");
}
