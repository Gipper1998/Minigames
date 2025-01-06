package me.gipper1998.minigames.games.buildit;

/***
 * Word file to pick a random list of words from yml.
 */

import me.gipper1998.minigames.utils.FileBuilder;

import java.util.List;
import java.util.Random;

public class BuildItWordManager
{
    private static BuildItWordManager wm;
    private FileBuilder words;
    private String prevWord;

    // Constructor and instance.
    public BuildItWordManager()
    {
        this.words = new FileBuilder("buildit\\words.yml");
        this.prevWord = "";
    }

    public static BuildItWordManager getInstance()
    {
        if (wm == null)
        {
            wm = new BuildItWordManager();
        }

        return wm;
    }

    // In case words need to be reloaded.
    public void reloadWords()
    {
        words.reloadConfig();
    }

    // Function to get random word.
    public String getRandomWord()
    {
        // Get all words and
        List<String> allWords =  words.getConfig().getStringList("Words");
        Random rand = new Random();
        int temp = rand.nextInt(allWords.size());
        String newWord = allWords.get(temp);

        // If no previous word, make it the word.
        if (prevWord.isEmpty())
        {
            prevWord = newWord;
            return newWord;
        }

        // Otherwise make sure word is new.
        else
        {
            if (newWord.equalsIgnoreCase(prevWord))
            {
                return getRandomWord();
            }
            else
            {
                prevWord = newWord;
                return newWord;
            }
        }
    }
}
