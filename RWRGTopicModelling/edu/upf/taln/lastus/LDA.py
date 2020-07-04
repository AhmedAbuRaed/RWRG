import random
import gensim
from gensim import corpora
from gensim.models import CoherenceModel
import csv

from gensim.models import ldamodel


def clusterTopicModeling(inputClusterdataPath, outputFilePath):
    referencesList = []
    with open(inputClusterdataPath, encoding='utf-8') as f:
        reader = csv.reader(f)
        for line in reader:
                tokens = line
                referencesList.append(tokens)
                if random.random() > .99:
                    print(tokens)

    referencesTokensList = [a[1:] for a in referencesList]
    dictionary = corpora.Dictionary(referencesTokensList)
    corpus = [dictionary.doc2bow(text) for text in referencesTokensList]
    if len(referencesList) > 2:
        coherence_values = []
        model_list = []

        for num_topics in range(2, len(referencesList), 1):
            model = gensim.models.ldamodel.LdaModel(corpus, num_topics=num_topics, id2word=dictionary, passes=15)
            model_list.append(model)
            coherencemodel = CoherenceModel(model=model, texts=referencesTokensList, dictionary=dictionary, coherence='c_v')
            coherence_values.append(coherencemodel.get_coherence())

        sorted_coherence_values, sorted_model_list = (list(t) for t in zip(*sorted(zip(coherence_values, model_list), reverse=True, key=lambda x: x[0])))
        optimal = 0
        while optimal + 1 < len(sorted_coherence_values) and sorted_coherence_values[optimal] - sorted_coherence_values[optimal + 1] < 0.03:
            optimal+=1

        ldamodel = sorted_model_list[optimal]

    elif len(referencesList) == 2:
        ldamodel = gensim.models.ldamodel.LdaModel(corpus, num_topics=2, id2word=dictionary, passes=15)
    elif len(referencesList) == 1:
        ldamodel = gensim.models.ldamodel.LdaModel(corpus, num_topics=1, id2word=dictionary, passes=15)
    else:
        print("There are no references in in the datapath provided ...")
        exit(-1)

    topics = ldamodel.print_topics()
    for topic in topics:
        print(topic)

    results = open(outputFilePath, 'w', encoding="utf8")
    for reference in referencesList:
        refID = reference[0]
        tokens = reference[1:]
        new_doc_bow = dictionary.doc2bow(tokens)
        document_topics = ldamodel.get_document_topics(new_doc_bow)
        line = ""
        for document_topic in document_topics:
            line += str(document_topic[0]) + "," + str(document_topic[1]) + "\t"
        results.write("\"" + refID + "\"\t" + line[:len(line) - 1] + "\n")
    results.close()