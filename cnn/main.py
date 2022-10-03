# -*- coding: utf-8 -*-
"""
Created on Thu Jan 28 00:44:25 2021

@author: chakati
"""
import cv2
import numpy as np
import os
import tensorflow as tf
import csv
import re

# import the handfeature extractor class
from frameextractor import frameExtractor
from handshape_feature_extractor import HandShapeFeatureExtractor

try:
    tf_gpus = tf.config.list_physical_devices('GPU')
    for gpu in tf_gpus:
        tf.config.experimental.set_memory_growth(gpu, True)
except:
    pass


# Helper classes and functions
class GestureInfo:
    # GestureInfo("FanOn", "FanOn", "11")

    def __init__(self, gestureId, gestureName, outputLabel):
        self.gestureId = gestureId
        self.gestureName = gestureName
        self.outputLabel = outputLabel


class GestureFeature:

    def __init__(self, gestureInfo: GestureInfo, extractedFeatures):
        self.gestureInfo = gestureInfo
        self.extractedFeatures = extractedFeatures


def extractFeature(path, inputFile, midFrameCounter):
    middleImage = cv2.imread(frameExtractor(
        path + inputFile, path + "frames/", midFrameCounter), cv2.IMREAD_GRAYSCALE)
    featureExtracted = HandShapeFeatureExtractor.extract_feature(
        HandShapeFeatureExtractor.get_instance(), middleImage)
    return featureExtracted


def getGestureByFileName(gestureFileName):
    for x in gestureData:
        if x.gestureId == gestureFileName.split('_')[0]:
            return x
    return None


gestureData = [
    GestureInfo("Num0", "0", "0"),
    GestureInfo("Num1", "1", "1"),
    GestureInfo("Num2", "2", "2"),
    GestureInfo("Num3", "3", "3"),
    GestureInfo("Num4", "4", "4"),
    GestureInfo("Num5", "5", "5"),
    GestureInfo("Num6", "6", "6"),
    GestureInfo("Num7", "7", "7"),
    GestureInfo("Num8", "8", "8"),
    GestureInfo("Num9", "9", "9"),
    GestureInfo("FanDown", "Decrease Fan Speed", "10"),
    GestureInfo("FanOn", "FanOn", "11"),
    GestureInfo("FanOff", "FanOff", "12"),
    GestureInfo("FanUp", "Increase Fan Speed", "13"),
    GestureInfo("LightOff", "LightOff", "14"),
    GestureInfo("LightOn", "LightOn", "15"),
    GestureInfo("SetThermo", "SetThermo", "16")
]

# =============================================================================
# Get the penultimate layer for trainig data
# =============================================================================
# your code goes here
# Extract the middle frame of each gesture video

featureVectorList = []
train_data_path = "traindata/"
count = 0
for file in os.listdir(train_data_path):
    # in our path we have videos and folder called frames so we want to take every thing but do not take frames folder
    if not file.startswith('frames'):
        featureVectorList.append(GestureFeature(getGestureByFileName(file),
                                                extractFeature(train_data_path, file, count)))
        count = count + 1


# =============================================================================
# Recognize the gesture (use cosine similarity for comparing the vectors)
# =============================================================================

def gesture_detection(gesture_folder_path, gesture_file_name, mid_frame_counter):
    """
        using train feature vector for all training data, compare the a given  test video frame feature vector
        with all the feature vectors for the training data using cosine similarity
        to decide the label of the gesture in the that test video
    """
    video_feature = extractFeature(gesture_folder_path, gesture_file_name, mid_frame_counter)

    flag = True
    num_mutations = 0
    gestureInfo: GestureInfo = GestureInfo("", "", "")
    while flag and num_mutations < 5:
        similarity = 1
        position = 0
        index = 0
        for featureVector in featureVectorList:
            cosine_similarity = tf.keras.losses.cosine_similarity(video_feature, featureVector.extractedFeatures, axis=-1)
            if cosine_similarity < similarity:
                similarity = cosine_similarity
                position = index
            index = index + 1
        gestureInfo = featureVectorList[position].gestureInfo
        flag = False
        if flag:
            num_mutations = num_mutations + 1
    return gestureInfo

# =============================================================================
# Get the penultimate layer for test data
# =============================================================================
# your code goes here
# Extract the middle frame of each gesture video

test_data_path = "test/"
test_count = 0
with open('results.csv', 'w', newline='') as results_file:
    fields_names = [
        'Gesture_Video_File_Name', 'Gesture_Name',
        'Output_Label']
    data_writer = csv.DictWriter(results_file, fieldnames=fields_names)
    data_writer.writeheader()

    for test_file in os.listdir(test_data_path):
        if not test_file.startswith('frames'):
            recognized_gesture_detail = gesture_detection(test_data_path, test_file, test_count)
            test_count = test_count + 1

            data_writer.writerow({
                'Gesture_Video_File_Name': test_file,
                'Gesture_Name': recognized_gesture_detail.gestureName,
                'Output_Label': recognized_gesture_detail.outputLabel})