これは、JHawk氏が作成したピクミン2の洞窟生成シミュレーションプログラム"CaveGen"のreadme.txtを日本語に翻訳したものです。


オリジナル
GitHub:
https://github.com/JHaack4/CaveGen


-----------------------------------------------------------------------------------------------------

このツールは自由に使用・修正していただいて構いません。
質問があればPikmin SpeedrunningかHocotate Hacker Discordにお願いします。
このツールはコマンドラインから実行する必要があります。

コンパイルの仕方:
javac *.java --release 8 && jar cmf manifest.mf CaveGen.jar *.class
実行の仕方:
java CaveGen [引数]
JARファイルの実行の仕方:
java -jar CaveGen.jar [引数]

引数:
出力(必須) - 画像が保存されるフォルダを設定します。
	seed - 各シード値に対して、"12345678"のようなフォルダが生成されます。
	cave - 各階層に対して、"BK-4"のようなフォルダが生成されます。
	both - 両方のフォルダが作成されます。
	none - 画像は生成されません。(統計情報を収集するためのツールの動作が非常に速くなります)
洞窟(必須) - 生成される洞窟を設定します。
	テキストファイルから読み込む: 例: caveinfo.txt
	グループ: "cmal"(チャレンジモード) "story"(本編) "both"(両方) を設定することで、ゲームの全ての階層を生成します。
	洞窟の省略形: 例: BK, SCx, CH1, CH2など。
階層の番号 (グループを使用しない場合は必須) - 生成する階層を設定します。0を設定した場合は洞窟全体になります。
seed - 使用するシード値を設定します(Meeoのシード値設定コードに対応します)。
num - 各階層に生成されるインスタンスの数(使用する際は注意してください。)
consecutiveSeeds(連続したシード値) - rand()関数によって生成されたシード値を順にチェックします(TASに便利です)
challengeMode - 通常モードで穴とアイテムを生成します。(これは正しく自動的に設定されます)
storyMode - ハードモードで穴やアイテムを生成します。
noImages - 画像を出力しません。
noPrints - コンソールに情報が出力されません。
noStats - !reportsフォルダに統計レポートが生成されません。
region [us|jpn|pal] - お宝のアイコンが変更されます。(デフォルトはusです)
251 - pikmin 251の洞窟を使用します。
caveInfoReport - 階層に関する全ての情報を含んだ画像を生成します。
drawSpawnPoints - 物体が生成される位置を描画します。
drawSpawnOrder - 物体が生成される順序を描画します。
drawAngles - 物体が向いている角度を描画します。
drawDoorIds - ゲートのIDを描画します。
drawTreasureGauge - 5つのリングを描画して、お宝ゲージの読み取りを判断するのを助けます。
drawHoleProbs - チャレンジモードでの穴の位置の条件付き確率を推定します
drawWayPoints - waypointのグラフを描画します。
drawWPVertexDists - 各waypointからポッドまでの距離を描画します。
drawWPEdgeDists - waypointのグラフの各辺に沿った距離を描画します。
drawAllWayPoints - 上に挙げた3つのオプションを全て有効にします。
drawScores - お宝や穴に対するゲーム内部のスコア算出関数を描画します。
drawDoorLinks - 距離に対するゲームの内部スコアを描画します。
drawEnemyScores - 敵に対するゲームの内部スコアを描画します。
drawUnitHoleScores - 穴のフェーズに対するユニットのスコアとゲートのスコアを描画します。
drawUnitItemScores - アイテムのフェーズに対するユニットのスコアとゲートのスコアを描画します。
drawAllScores - 上に挙げた5つのオプションを全て有効にします。
drawNoWaterBox - 青い水槽を描画しません。
drawNoPlants - 植物のアイコンを描画しません。
drawNoFallType - 落下物の指標を描画しません。
drawNoGateLife - ゲートのライフを描画しません。
drawNoObjects - オブジェクトを描画しません。


例:
java -jar CaveGen.jar seed story -seed 0x12345678 -drawSpawnPoints
  本編のすべての階層の画像を、指定したシード値(0x12345678)で生成します。
java -jar CaveGen.jar cave BK 4 -num 100 -seed 0 -consecutiveSeeds
  0に続くシード値をチェックしながら、BK4の100個のインスタンスの画像を生成します。
java -jar CaveGen.jar none CH12 0 -num 10000
  コンクリート迷路の10000個のインスタンスの統計情報を、画像無しで生成します。
java -jar CaveGen.jar caveinfo.txt 0
  caveinfo.txtの洞窟全体を生成します。


生成位置のキー:
0 ピンク = 弱い敵(size = 半径, num rings = 最大スポーン数)
1 赤 = 強い敵
2 オレンジ = お宝
4 緑 = 穴の位置
5 グレー = ゲート
6 ライトグリーン = 植物
7 ティール色 = 開始位置
8 紫 = 特別な敵
9 青 = alcove

落下物のキー:
0 無し = 落ちてこない
1 紫 = 何かが近づくと落ちてくる
2 青 = ピクミンが近づくと落ちてくる
3 赤 = リーダーが近づくと落ちてくる
4 オレンジ = 運搬中のピクミンが近づくと落ちてくる
5 緑 = 紫ピクミンの落下の振動が近いと落ちてくる


ユニットのタイプのキー:
0 alcove
1 部屋
2 廊下


詳細情報:
https://pikmintkb.com/wiki/Cave_generation_parameters


製作者:
JHawk, 11/4/2019
special thanks to ice cube, Meeo, and Espyo for their help

生成アルゴリズムについて知りたい場合、CaveGen.javaのコードを読むか、
https://www.twitch.tv/videos/499998582を見るといいでしょう。
あなたの理解度をテストするために、以下のクイズに挑戦してみてください。

