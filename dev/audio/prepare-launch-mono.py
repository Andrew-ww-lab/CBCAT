"""Preserve the original ignition clip, downmixing for Minecraft positional playback.

Run from the repository root with soundfile available (development-only dependency).
The original stereo recording is deliberately retained unchanged.
"""
from pathlib import Path
import soundfile as sf

sounds = Path('src/main/resources/assets/cbcatfix/sounds')
source = sounds / 'missile_launch.ogg'
target = sounds / 'missile_launch_mono.ogg'
samples, sample_rate = sf.read(source, always_2d=True)
sf.write(target, samples.mean(axis=1), sample_rate, format='OGG', subtype='VORBIS')
info = sf.info(target)
assert info.channels == 1
assert info.frames == samples.shape[0]
print(f'{target}: mono, {info.duration:.3f}s, {info.samplerate} Hz')
