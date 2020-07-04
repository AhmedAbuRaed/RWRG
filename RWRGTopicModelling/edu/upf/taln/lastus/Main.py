import sys
import os

from edu.upf.taln.lastus.LDA import clusterTopicModeling


def main():
    if len(sys.argv) == 3:
        inputFolder = sys.argv[1]
        outputFolder = sys.argv[2]

        for entry in os.scandir(inputFolder):
            print(entry.name)
            clusterTopicModeling(entry.path, outputFolder + "/" + entry.name)
    else:
        print("Please pass the dataset folder Path alongside the output folder path ...")
if __name__ == "__main__":
    main()
