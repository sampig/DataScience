SAP Task-1 NLP
=========================
Author: [Chenfeng ZHU](mailto:zhuchenf@gmail.com)


## Table of Content

* [Tasks](#tasks)
    - [Words and Phrases Distribution](#words-and-phrases-distribution)
    - [Phrases Distribution](#phrases-distribution)
    - [Sentiments of Sentences](#sentiments-of-sentences)
    - [Topics Analysis via LDA](#topics-analysis-via-lda)
* [Manual](#manual)
* [Libraries](#libraries)
* [References](#references)



## Tasks

Task list:
1. Words and Phrases distribution.
2. Named entity identification.
3. Sentiments of sentences.
4. Topics analysis via LDA.


### Words and Phrases Distribution

Calculate:
1. Raw Word Frequency.
2. Word Frequency after removing punctuation, stemming and stop words.
3. Bigram: Term Frequency.
4. Trigram: Term Frequency.
5. 4-gram: Term Frequency.
6. Skip-2-gram: Term Frequency.
7. Skip-3-gram: TermFrequency.

```
Number of total words: 48071
Number of distinct raw words: 9012
Number of words: 5468
Number of meaningful words: 3592
```
```
Number of phrases with 2-gram: 26058
Number of phrases with 3-gram: 38419
Number of phrases with 4-gram: 39394
Number of phrases with skip-2-gram: 27613
Number of phrases with skip-3-gram: 27273
Number of phrases: 1743
```

Saving some distributions as word cloud diagrams.

Code: _org.zhuzhu.application.sap.DistributionApplication_

#### Comments

Just as we could predict, stop words always have the highest frequency. Removing them, the names of characters will occur mostly. Something else like verbs, locations will also be in quite amount.
The main character might be Anna and Pierre.

Different tokenization and vocabularies will also influence the exploration.

#### Improvements

If we have many documents, we could use TF-IDF to detect valid or meaningful phrases. Or maybe we could try to split the chapters manually by "CHAPTER" into several documents.

Based on a single document, we could use co-occurrences of words to extract some key phrases.

The algorithms of these two methods could be found in the papers in [References](#references).


### Named entity identification

We could use two libraries, Apache OpenNLP and Stanford NLP to detect entities. (Unfortunately, it requires too many memories using Stanford NLP and it often runs out of 4GB memories. I failed to run this part of my program.)

Some results about locations and characters using Apache OpenNLP are shown below:
```
Austria
Russia
Europe
Vienna
French
Moscow
Paris
```
```
Anna
Alexander
Hardenburg
Will
Lise
Annette
Hippolyte
Pierre
```

Code: _org.zhuzhu.application.sap.EntityDetector_, _org.zhuzhu.application.sap.EntityDetector2_

#### Comments

Obviously, they are not totally correct. Some places or names are wrong, and some other are missing.
And the language libraries provided by Apache OpenNLP are not complete. They provide only several languages.

#### Improvements

If we want to develop our own machine learning algorithm for NE, we could use supervised learning which requires lots of annotated documents.

We could list some sample sentences from the document and let user to mark the names of the entities in those sentences. Then we could use them as a training data set.

However, according to Apache OpenNLP, "_The training data should contain at least 15000 sentences to create a model which performs well._" It will be a huge work for a user to mark the name of entity manually from sample sentences.

We could also use unsupervised learning. Two algorithms could be found in two papers in [References](#references).

Using a web-service tool, TAGME, is also an interesting test. But it requires a key which can easily be achieved after registration.
Send a text via web request and get the response. TAGME will make a tag for those potential entities and their linked entity names including their corresponding probability. Data could be found in _tagme.out_.


### Sentiments of sentences

Use supervised machine learning, classification, algorithms to classify sentiments of sentences.

The training data comes from [Kaggle project: "_Sentiment Analysis: Emotion in Text_"](https://www.kaggle.com/c/sa-emotions/).
It provides sentences with corresponded emotion labels.
Samples:
```
sentiment,content
empty,@tiffanylue i know  i was listenin to bad habit earlier and i started freakin at his part =[
sadness,Layin n bed with a headache  ughhhh...waitin on your call...
sadness,Funeral ceremony...gloomy friday...
```

Use the training data to build a classifier with a classification algorithm such as Decision Tree, Naive Bayes, kNN or SVM.
Use bag-of-words as features(due to the time and memory limit, I did not use N-Grams or other methods) and use TF &sdot; IDF = f/sum(f) &sdot; log(D/d) to calculate the values as a vector after normalization.

Evaluations for one of the models:
```
Results
======
Correctly Classified Instances         474                1.58   %
Incorrectly Classified Instances     29526               98.42   %
Kappa statistic                          0.0062
Mean absolute error                      0.1514
Root mean squared error                  0.3882
Relative absolute error                116.3341 %
Root relative squared error            152.1908 %
Total Number of Instances            30000     
```

Use one of the best results as the model and apply it to other sentences in the document. Of course, this result cannot be acceptable.

The prediction of sentiments of some sentences:
```
The predicted value of instance-0 'war and peace book one 1805': anger
The predicted value of instance-1 ' leo tolstoy': worry
...
The predicted value of instance-15 ' if you have nothing better to do count or prince and if the prospect of spending an evening with a poor invalid is not too terrible i shall be very charmed to see you tonight between 7 and 10 annette scherer': anger
The predicted value of instance-16 ' ': worry
The predicted value of instance-17 ' heavens what a virulent attack replied the prince not in the least disconcerted by this reception': anger
The predicted value of instance-18 ' he had just entered wearing an embroidered court uniform knee breeches and shoes and had stars on his breast and a serene expression on his flat face': boredom
The predicted value of instance-19 ' he spoke in that refined french in which our grandfathers not only spoke but thought and with the gentle patronizing intonation natural to a man of importance who had grown old in society and at court': fun
```

Code: _org.zhuzhu.application.sap.EntityDetector_, _org.zhuzhu.application.sap.EntityDetector2_

#### Comments

The results are poor.
Due to the time limits, I did not test different classification algorithms or their different parameters.
Neither, I did not provide the commands for users to use different classifiers to build the model. It is not flexible and user-friendly.

Another problem is that I have not checked the distribution and properties of training data from the web.

#### Improvements

The classification algorithm must be improved:
1. I should change the methods for features generation.
Not only use bag-of-words, but also use 2-grams, 3-grams, skip-2-grams etc. to generate the features.
2. Reduce the features using techniques like WrapperSubsetEval, InfoGainAttributeEval and CorrelationAttributeEval provided by Weka. Some other tools or libraries also provide similar functions.
3. Try different classifications and different parameters.
4. Some sentiments dictionaries would also be helpful.


### Topics Analysis via LDA

Using LDA(Latent Dirichlet Allocation) algorithms to analyze topics of chapters. Some libraries can be found in [References](#references).

Unfinished.



## Manual

Compile the sources:
```bash
mvn install
```

Run the distribution:
```bash
java -classpath target/uber-sap-0.0.1-SNAPSHOT.jar org.zhuzhu.application.sap.DistributionApplication -i INPUT_FILE -op OUTPUT_FILE_PATH
```

Run the entity detection:
```bash
java -classpath target/uber-sap-0.0.1-SNAPSHOT.jar org.zhuzhu.application.sap.EntityDetector -i INPUT_FILE -op OUTPUT_FILE_PATH
java -classpath target/uber-sap-0.0.1-SNAPSHOT.jar org.zhuzhu.application.sap.EntityDetector2 -i INPUT_FILE -op OUTPUT_FILE_PATH
```

Run the sentiments detection of sentences:
```bash
# Build classifier:
java -classpath target/uber-sap-0.0.1-SNAPSHOT.jar org.zhuzhu.application.sap.learning.SentimentsTrainer -tr TRAIN_DATA -te TEST_DATE -op OUTPUT_FILE_PATH -ct NB|DT|KNN
# Run sentiments classification:
java -Xmx4096m -classpath target/uber-sap-0.0.1-SNAPSHOT.jar org.zhuzhu.application.sap.learning.SentimentsTrainer -i INPUT_FILE -im INPUT_MODEL -if INPUT_FEATURES -op OUTPUT_FILE_PATH
```



## Libraries

The libraries are being used in this application(details can be found in _pom.xml_):
1. Apache Lucene
2. Kumo
3. Apache OpenNLP
4. Stanford CoreNLP
5. Apache Httpcomponents
6. Javax Json
7. Weka



## References

- [Apache Lucene](https://lucene.apache.org/)
- [Kumo - Java Word Cloud](https://github.com/kennycason/kumo)
- Mikolov, Tomas, et al. "_Distributed representations of words and phrases and their compositionality._" Advances in neural information processing systems. 2013.
- Matsuo, Yutaka, and Mitsuru Ishizuka. "_Keyword extraction from a single document using word co-occurrence statistical information._" International Journal on Artificial Intelligence Tools 13.01 (2004): 157-169.
- [Apache OpenNLP](http://opennlp.apache.org/)
- [Pre-trained models for the OpenNLP 1.5 series](http://opennlp.sourceforge.net/models-1.5/)
- [Stanford CoreNLP - Natural language software](https://stanfordnlp.github.io/CoreNLP/)
- Collins, Michael, and Yoram Singer. "_Unsupervised models for named entity classification._" 1999 Joint SIGDAT Conference on Empirical Methods in Natural Language Processing and Very Large Corpora. 1999.
- Yangarber, Roman, Winston Lin, and Ralph Grishman. "_Unsupervised learning of generalized names._" Proceedings of the 19th international conference on Computational linguistics-Volume 1. Association for Computational Linguistics, 2002.
- [TAGME](https://tagme.d4science.org/tagme/)
- [Kaggle project: "_Sentiment Analysis: Emotion in Text_"](https://www.kaggle.com/c/sa-emotions/)
- [Weka 3: Data Mining Software in Java](www.cs.waikato.ac.nz/ml/weka/index.html)
- [Deep Learning for Java](https://deeplearning4j.org/)
- Lots of other papers about classifications.
- [Opinion Mining, Sentiment Analysis, and Opinion Spam Detection](https://www.cs.uic.edu/~liub/FBS/sentiment-analysis.html)
- Liu, B. "_Sentiment Analysis: mining sentiments, opinions, and emotions._" Cambridge UP (2015).
- [JGibbLDA](http://jgibblda.sourceforge.net/)
- [jLDADMM: A Java package for the LDA and DMM topic models](https://github.com/datquocnguyen/jLDADMM)


