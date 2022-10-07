# -*- coding: utf-8 -*-
"""
Created on Thu Jan 28 00:44:25 2021

@author: chakati
"""
import cv2
import os
import tensorflow as tf
import csv

# import the handfeature extractor class
from frameextractor import frameExtractor
from handshape_feature_extractor import HandShapeFeatureExtractor

# Helper classes and functions
class GestureInfo:
    """
    holds information for each gesture
    ex: GestureInfo("FanOn", "FanOn", "11")
    """

    def __init__(self, gesture_id, gesture_name, gesture_output_label):
        self.gesture_id = gesture_id
        self.gesture_name = gesture_name
        self.gesture_output_label = gesture_output_label


class Gesture:
    """
    holds gesture information and extracted feature
    """

    def __init__(self, gesture_info: GestureInfo, extracted_feature):
        self.gesture_info = gesture_info
        self.extracted_feature = extracted_feature


def extract_feature(path, input_file_name, mid_frame):
    """
    extract middle frame feature
    """
    middle_frame_image = cv2.imread(frameExtractor(path + input_file_name, path + "frames/", mid_frame), cv2.IMREAD_GRAYSCALE)
    extracted_feature = HandShapeFeatureExtractor.extract_feature(HandShapeFeatureExtractor.get_instance(), middle_frame_image)
    return extracted_feature


def get_gesture_info_from_file_name(gesture_file_name):
    """
    get gesture info object from gesture file name
    """
    for x in all_gestures:
        if x.gesture_id == gesture_file_name.split('_')[0]:
            return x
    return None


all_gestures = [
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

feature_vectors = []
train_data_path = "traindata/"
count = 0
for file_name in os.listdir(train_data_path):
    if not file_name.startswith('frames'):
        feature_vectors.append(Gesture(get_gesture_info_from_file_name(file_name), extract_feature(train_data_path, file_name, count)))
        count += 1


# =============================================================================
# Recognize the gesture (use cosine similarity for comparing the vectors)
# =============================================================================

def gesture_prediction(test_gesture_folder_path, test_gesture_file_name, mid_frame):
    """
    for each test gesture feature vector, compare against all training gesture feature vectors using
    cosine similarity methods to determine output label of test gesture.
    """
    test_middle_frame_feature = extract_feature(test_gesture_folder_path, test_gesture_file_name, mid_frame)

    flag = True
    num_mutations = 0
    predicted_gesture_info: GestureInfo = GestureInfo("", "", "")
    while flag and num_mutations < 5:
        similarity = 1
        pos = 0
        idx = 0
        for feature_vector in feature_vectors:
            cosine_similarity = tf.keras.losses.cosine_similarity(test_middle_frame_feature, feature_vector.extracted_feature, axis=-1)
            if cosine_similarity < similarity:
                similarity = cosine_similarity
                pos = idx
            idx += 1
        predicted_gesture_info = feature_vectors[pos].gesture_info
        flag = False
        if flag:
            num_mutations = num_mutations + 1
    return predicted_gesture_info

# =============================================================================
# Get the penultimate layer for test data
# =============================================================================
# your code goes here
# Extract the middle frame of each gesture video

if __name__ == "__main__":
    test_data_path = "test/"
    test_count = 0
    with open('Results.csv', 'w', newline='') as results_file:
        fields_names = [
            'Output Label'
        ]
        data_writer = csv.DictWriter(results_file, fieldnames=fields_names)

        for test_gesture_file_name in os.listdir(test_data_path):
            if not test_gesture_file_name.startswith('frames'):
                predicted_gesture_info = gesture_prediction(test_data_path, test_gesture_file_name, test_count)
                test_count = test_count + 1

                data_writer.writerow({
                    'Output Label': predicted_gesture_info.gesture_output_label
                })