# Content

This project has one goal:

Guess the intent of a short sentence entered by a human by analysing it with machine learning algorithms.

To achieve this there are currently following steps to follow:

- Pick some source data with arbitrary text of your domain. You can achieve a better result if you use text from your
 domain in your preferred language.

- Start the machine learning process. This will take some time but you have to run it only once.

- Define the intents you want to detect.

- Use it by simple http requests which will return you the guessed intent in from of a json.

## Transform data

Currently there is a class TransformWikiText which lets you transform the latest texts from wikipedia in form of an
xml file. The transformed file consists of simple text with a senctence on each line.

## Learning

Start learning the transformed text by executing the class LearnMain with the file name as the only argument. It will
take a while. The output is a file with the suffix `.model`.

## Define intents
In a new file with the name `intents.txt` beside the code you define your intents. It may look like this:

```
invoice: invoice currency payment bank
delivery: delivery notice shipment arrival
customer care: problem error warning incident
```

The part before the colon defines the name of the intent. The words after the colon define the space of it.

## Use it

Currently there is only an interface working over standard in:

- Type one word: It will show you the nearest words
- Type two words: It will show you how similar those words are
- Type more words: It will show you the intents ordered by probability

In the future there will probably be a web api like this:

If you start the server there will be a simple web api where you can post a sentence to the url
`http://host:80/queryintent`. It will then return a json object that looks like this:

```
{
  "invoice": 0.5373,
  "delivery": 0.0023,
  "customer care": 0.0001
}
```

It was probably a sentence about an invoice.