When it comes to relational databases, I love using PostgreSQL. It's fast, featureful, and very well documented. Postgres provides executables for backing up and restoring databases, called `pg_dump` and `pg_restore` respectively. You can read about them [here](https://www.postgresql.org/docs/current/backup-dump.html). For a long time I was typing these commands manually, but then I started desiring a script that could somewhat automate the process with timestamp filenames and confirmation prompts. So I started writing a basic shell script. But of course, writing shell scripts is a challenge for the majority of developers who aren't extremely familiar with the syntax. I'm certainly not a Bash expert. Whenever I find myself writing loops or conditional expressions, I quickly grow tired and consider alternatives.

In the past this was Python, but since I began falling head over heels for Clojure, I much prefer it over other languages. Luckily, the Clojure community has some amazing open-source contributors like [Michiel Borkent AKA @borkdude](https://github.com/borkdude). Michiel created an amazing tool called [Babashka](https://babashka.org) which (from what I understand) compiles a subset of Clojure into Bash, making it possible to write Clojure code in scripting situations without the startup penalty of JVM Clojure. This means you can write Clojure programs that execute seemingly instantaneously. Let's look at an example.

Typically, Babashka executes code inside of standard `.clj` files. As of version `0.4.0`, Babashka ships with a feature called "tasks" which executes Clojure code inside of the `bb.edn` file which Babashka uses for configuration. For example, a `bb.edn` file with a simple task that removes a local directory called `target` could look like this:

```clojure
{:min-bb-version "0.4.0",
 :tasks {clean (do (println "Removing target folder.")
                   (babashka.fs/delete-tree "target"))}}
```

And you could run this task with `bb run clean` or the shorter version `bb clean`. Running this task on my computer takes about 26 milliseconds.

```
❯ time bb clean
Removing target folder.
bb clean  0.02s user 0.01s system 101% cpu 0.026 total
```

To get a sense of how fast Babashka starts up compared to JVM Clojure, this is how long it takes JVM Clojure to print out a single line of text to the terminal:

```
❯ time clojure -M test.clj
The JVM startup penalty is something fierce!
clojure -M test.clj  2.85s user 0.15s system 188% cpu 1.594 total
```

1594 milliseconds! Not exactly the performance you want for simple and short Bash-like scripts.

So after playing around with Babashka for a while, I decided to write some Babashka tasks that could backup and restore my databases for me. Here's what I came up with:

```clojure
; run `bb backup` to backup database
; run `bb restore` to restore latest backup

{:min-bb-version "0.4.0",
 :tasks {; CONSTANTS
         db-name "dev_blog",
         backup-dir "backups",
         now (str (java.time.LocalDateTime/now)),
         ; TASKS
         create-backup-dir {:depends [backup-dir], :task (babashka.fs/create-dirs backup-dir)},
         backup {:depends [db-name now backup-dir create-backup-dir],
                 :task (let [backup-path (str backup-dir "/" db-name "_backup:" now ".dump")
                             backup-command (str "pg_dump --format=custom " db-name)]
                         (do (println (str "Backing up database: " db-name))
                             (println (str "Backup location: " backup-path))
                             (if (-> (shell {:out backup-path} backup-command)
                                     (:exit)
                                     (= 0))
                               (println "Backup successful! 🚀")
                               (println "Errors occurred during backup ⚠️"))))},
         restore {:depends [db-name backup-dir],
                  :task (let [latest-backup-path (-> (babashka.fs/list-dir backup-dir)
                                                     (sort)
                                                     (reverse)
                                                     (first)
                                                     (str))
                              restore-command (str "pg_restore --clean --dbname=" db-name " " latest-backup-path)
                              do-restore? (do (println "Latest backup: " latest-backup-path)
                                              (println "Restore command: " restore-command)
                                              (println "Restore this backup? (y/n): ")
                                              (= (first (line-seq (clojure.java.io/reader *in*))) "y"))]
                          (if do-restore?
                            (do (println "Restoring database... ")
                                (if (-> (shell restore-command)
                                        (:exit)
                                        (= 0))
                                  (println "Restoration successful! 🚀")
                                  (println "Errors occurred during restoration ⚠️")))
                            (println "Skipping restoration...")))}}}
```

Now I can simply run `bb backup` and `bb restore` when I want to interact with my backups. It's so much easier than manually typing in the `pg_dump` and `pg_restore` commands, and in my opinion this code is **much** more readable than a shell script that could do the equivalent. I'm definitely biased because I love Clojure and dislike shell scripting, but I think many beginner programmers would also agree!

Here's what it looks like when I want to backup a database:

```
❯ bb backup
Backing up database: dev_blog
Backup location: backups/dev_blog_backup:2021-07-23T14:24:22.754740.dump
Backup successful! 🚀
```

And when I want to restore from a backup:

```
❯ bb restore
Latest backup:  backups/dev_blog_backup:2021-07-23T14:24:22.754740.dump
Restore command:  pg_restore --clean --dbname=dev_blog backups/dev_blog_backup:2021-07-23T14:24:22.754740.dump
Restore this backup? (y/n):
y
Restoring database...
Restoration successful! 🚀
```

An overview of the `pg_*` commands I'm using within this program:
- `pg_dump --format=custom <db-name>`: I'm using the `--format=custom` flag because this saves the backup in a format that makes it trivally easy to restore using `pg_restore`. I tried using the default `--format=plain` which saves the backup as a plain text SQL file, but you can't actually restore these backups using `pg_restore`. You have to pipe the file into the `psql` command which I found difficult to do in Babashka. `--format=custom` seems to be the superior choice for ease of use anyway.
- `pg_restore --clean --dbname=<dn-name> <path-to-backup>`: I'm using the `--clean` flag here because this drops the database before the backup is loaded. Since everything about the database is contained within the backup, this is the behavior I want.

For short and sweet Bash-like scripts, Babashka is incredible. If you also enjoy using Clojure and need to write shell scripts, definitely consider using it! And feel free to take these Babashka tasks for backing up your own PostgreSQL databases!

References:  
[Gist](https://gist.github.com/stelcodes/a8c3b8f4b9c07ef784675750ab91079e)  
[Babashka Show & Tell](https://github.com/babashka/babashka/discussions/929)
