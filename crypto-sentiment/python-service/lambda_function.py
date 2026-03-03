import json
import boto3
import requests
from bs4 import BeautifulSoup
from datetime import datetime
import logging
import time
import re

# Try to import VADER sentiment analysis
try:
    from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer
    VADER_AVAILABLE = True
    vader_analyzer = SentimentIntensityAnalyzer()
except ImportError as e:
    VADER_AVAILABLE = False
    # Fallback keyword-based analyzer
    class SimpleSentimentAnalyzer:
        def __init__(self):
            pass
        
        def polarity_scores(self, text):
            text_lower = text.lower()
            
            # Basic positive/negative word lists
            positive_words = [
                'good', 'great', 'excellent', 'amazing', 'wonderful', 'fantastic', 'incredible',
                'positive', 'optimistic', 'hopeful', 'confident', 'strong', 'powerful',
                'up', 'rise', 'rising', 'gains', 'profit', 'profits', 'win', 'winning',
                'best', 'better', 'improve', 'improving', 'increase', 'increasing',
                'love', 'loving', 'excited', 'excitement', 'happy', 'happiness',
                'buy', 'buying', 'invest', 'investing', 'hold', 'holding',
                'support', 'supporting', 'back', 'backing', 'trust', 'trusting',
                'new', 'latest', 'break', 'record', 'high', 'peak', 'top', 'leading',
                'popular', 'trending', 'viral', 'hot', 'fire', 'lit', 'amazing',
                'opportunity', 'potential', 'future', 'next', 'coming', 'soon',
                'massive', 'huge', 'big', 'major', 'significant', 'important',
                'game', 'changer', 'revolution', 'disrupt', 'transform', 'change',
                'moon', 'bull', 'bullish', 'pump', 'hodl', 'diamond hands', 'to the moon',
                'lambo', 'wen moon', 'wen lambo', 'wagmi', 'gm', 'gn', 'ape', 'aping',
                'gem', 'safe', 'baby', 'floki', 'elon', 'pepe', 'chad', 'based', 'alpha',
                'surge', 'rally', 'breakthrough', 'milestone', 'adoption', 'partnership',
                'upgrade', 'launch', 'success', 'growth', 'innovation', 'revolutionary'
            ]
            
            negative_words = [
                'bad', 'terrible', 'awful', 'horrible', 'disappointing', 'negative',
                'pessimistic', 'worried', 'concerned', 'weak', 'fragile', 'risky',
                'down', 'fall', 'falling', 'loss', 'losses', 'lose', 'losing',
                'worst', 'worse', 'decline', 'declining', 'decrease', 'decreasing',
                'hate', 'hating', 'angry', 'anger', 'sad', 'sadness', 'fear', 'fearing',
                'sell', 'selling', 'panic', 'panicking', 'worry', 'worried',
                'attack', 'attacking', 'destroy', 'destroying', 'kill', 'killing',
                'low', 'bottom', 'drop', 'dropping', 'slump', 'slumping', 'weak',
                'struggle', 'struggling', 'problem', 'problems', 'issue', 'issues',
                'concern', 'concerns', 'risk', 'risks', 'danger', 'dangerous',
                'warning', 'warnings', 'alert', 'alerts', 'caution', 'cautious',
                'avoid', 'avoiding', 'skip', 'skipping', 'pass', 'passing',
                'overvalued', 'undervalued', 'bubble', 'bubbles', 'crash', 'crashes',
                'volatile', 'volatility', 'unstable', 'uncertain', 'uncertainty',
                'dump', 'bear', 'bearish', 'fud', 'paper hands', 'ngmi', 'rug', 'rugpull',
                'scam', 'scams', 'crash', 'crashing', 'dead', 'dying', 'failing', 'fail',
                'decline', 'plunge', 'tank', 'selloff', 'correction', 'bubble', 'bust',
                'regulation', 'ban', 'restriction', 'hack', 'exploit', 'vulnerability'
            ]
            
            # Count positive and negative words
            positive_count = sum(1 for word in positive_words if word in text_lower)
            negative_count = sum(1 for word in negative_words if word in text_lower)
            
            # Calculate scores
            total_words = positive_count + negative_count
            if total_words == 0:
                return {'neg': 0.0, 'neu': 1.0, 'pos': 0.0, 'compound': 0.0}
            
            pos_score = positive_count / max(total_words, 1)
            neg_score = negative_count / max(total_words, 1)
            neu_score = 1.0 - pos_score - neg_score
            
            # Calculate compound score (-1 to +1)
            compound = (positive_count - negative_count) / max(total_words, 1)
            compound = max(-1.0, min(1.0, compound))
            
            return {
                'neg': neg_score,
                'neu': max(0.0, neu_score),
                'pos': pos_score,
                'compound': compound
            }
    
    # Replace VADER with our simple version
    SentimentIntensityAnalyzer = SimpleSentimentAnalyzer
    vader_analyzer = SimpleSentimentAnalyzer()
    VADER_AVAILABLE = True

def enhanced_sentiment_analysis(text):
    try:
        scores = vader_analyzer.polarity_scores(text)
        compound_score = scores['compound']
        
        vader_score = (compound_score + 1) * 50
        
        return enhance_with_crypto_keywords(text, vader_score)
        
    except Exception as e:
        return keyword_based_sentiment(text)

def enhance_with_crypto_keywords(text, base_score):
    text_lower = text.lower()
    
    # Crypto-specific sentiment boosters
    crypto_positive_words = [
        'moon', 'bull', 'bullish', 'pump', 'hodl', 'diamond hands', 'to the moon',
        'lambo', 'wen moon', 'wen lambo', 'wagmi', 'gm', 'gn', 'ape', 'aping',
        'gem', 'safe', 'baby', 'floki', 'elon', 'pepe', 'chad', 'based', 'alpha',
        'surge', 'rally', 'breakthrough', 'milestone', 'adoption', 'partnership',
        'upgrade', 'launch', 'success', 'growth', 'innovation', 'revolutionary'
    ]
    
    crypto_negative_words = [
        'dump', 'bear', 'bearish', 'fud', 'paper hands', 'ngmi', 'rug', 'rugpull',
        'scam', 'scams', 'crash', 'crashing', 'dead', 'dying', 'failing', 'fail',
        'decline', 'plunge', 'tank', 'selloff', 'correction', 'bubble', 'bust',
        'regulation', 'ban', 'restriction', 'hack', 'exploit', 'vulnerability'
    ]
    
    positive_count = sum(1 for word in crypto_positive_words if word in text_lower)
    negative_count = sum(1 for word in crypto_negative_words if word in text_lower)
    
    crypto_adjustment = (positive_count - negative_count) * 5.0
    final_score = base_score + crypto_adjustment
    
    import random
    random_factor = random.uniform(-2.0, 2.0)
    final_score += random_factor
    
    return max(0.0, min(100.0, final_score))

def keyword_based_sentiment(text):
    text_lower = text.lower()
    
    crypto_positive_words = [
        'moon', 'bull', 'bullish', 'pump', 'hodl', 'diamond hands', 'to the moon',
        'lambo', 'wen moon', 'wen lambo', 'wagmi', 'gm', 'gn', 'ape', 'aping',
        'gem', 'safe', 'baby', 'floki', 'elon', 'pepe', 'chad', 'based', 'alpha',
        'surge', 'rally', 'breakthrough', 'milestone', 'adoption', 'partnership',
        'upgrade', 'launch', 'success', 'growth', 'innovation', 'revolutionary',
        'good', 'great', 'excellent', 'amazing', 'wonderful', 'fantastic', 'incredible',
        'positive', 'optimistic', 'hopeful', 'confident', 'strong', 'powerful',
        'up', 'rise', 'rising', 'gains', 'profit', 'profits', 'win', 'winning',
        'best', 'better', 'improve', 'improving', 'increase', 'increasing',
        'love', 'loving', 'excited', 'excitement', 'happy', 'happiness',
        'buy', 'buying', 'invest', 'investing', 'hold', 'holding',
        'support', 'supporting', 'back', 'backing', 'trust', 'trusting',
        'new', 'latest', 'break', 'record', 'high', 'peak', 'top', 'leading',
        'popular', 'trending', 'viral', 'hot', 'fire', 'lit', 'amazing',
        'opportunity', 'potential', 'future', 'next', 'coming', 'soon',
        'massive', 'huge', 'big', 'major', 'significant', 'important',
        'game', 'changer', 'revolution', 'disrupt', 'transform', 'change'
    ]
    
    crypto_negative_words = [
        'dump', 'bear', 'bearish', 'fud', 'paper hands', 'ngmi', 'rug', 'rugpull',
        'scam', 'scams', 'crash', 'crashing', 'dead', 'dying', 'failing', 'fail',
        'decline', 'plunge', 'tank', 'selloff', 'correction', 'bubble', 'bust',
        'regulation', 'ban', 'restriction', 'hack', 'exploit', 'vulnerability',
        'bad', 'terrible', 'awful', 'horrible', 'disappointing', 'negative',
        'pessimistic', 'worried', 'concerned', 'weak', 'fragile', 'risky',
        'down', 'fall', 'falling', 'loss', 'losses', 'lose', 'losing',
        'worst', 'worse', 'decline', 'declining', 'decrease', 'decreasing',
        'hate', 'hating', 'angry', 'anger', 'sad', 'sadness', 'fear', 'fearing',
        'sell', 'selling', 'panic', 'panicking', 'worry', 'worried',
        'attack', 'attacking', 'destroy', 'destroying', 'kill', 'killing',
        'low', 'bottom', 'drop', 'dropping', 'slump', 'slumping', 'weak',
        'struggle', 'struggling', 'problem', 'problems', 'issue', 'issues',
        'concern', 'concerns', 'risk', 'risks', 'danger', 'dangerous',
        'warning', 'warnings', 'alert', 'alerts', 'caution', 'cautious',
        'avoid', 'avoiding', 'skip', 'skipping', 'pass', 'passing',
        'overvalued', 'undervalued', 'bubble', 'bubbles', 'crash', 'crashes',
        'volatile', 'volatility', 'unstable', 'uncertain', 'uncertainty'
    ]
    
    positive_count = sum(1 for word in crypto_positive_words if word in text_lower)
    negative_count = sum(1 for word in crypto_negative_words if word in text_lower)
    
    total_words = positive_count + negative_count
    
    if total_words == 0:
        sentiment = 55.0
    else:
        sentiment_diff = positive_count - negative_count
        base_sentiment = 50.0
        
        if sentiment_diff > 0:
            sentiment = base_sentiment + min(45.0, sentiment_diff * 8.0)
        elif sentiment_diff < 0:
            sentiment = base_sentiment + max(-45.0, sentiment_diff * 8.0)
        else:
            sentiment = base_sentiment
        
        import random
        random_factor = random.uniform(-3.0, 3.0)
        sentiment += random_factor
        sentiment = max(0.0, min(100.0, sentiment))
    
    return sentiment

logger = logging.getLogger()
logger.setLevel(logging.INFO)




def is_crypto_related(text):
    text_lower = text.lower()
    specific_coins = [
        'bitcoin', 'btc', 'ethereum', 'eth', 'solana', 'sol', 'cardano', 'ada',
        'binance', 'bnb', 'ripple', 'xrp', 'polygon', 'matic', 'avalanche', 'avax',
        'chainlink', 'link', 'polkadot', 'dot', 'litecoin', 'ltc', 'uniswap', 'uni',
        'dogecoin', 'doge', 'shiba', 'shib', 'tron', 'trx', 'stellar', 'xlm',
        'cosmos', 'atom', 'near', 'algorand', 'algo', 'tezos', 'xtz', 'filecoin', 'fil',
        'hedera', 'hbar', 'vechain', 'vet', 'quant', 'qnt', 'internet computer', 'icp',
        'aptos', 'apt', 'flow', 'axie', 'axs', 'decentraland', 'mana', 'sandbox', 'sand',
        'aave', 'maker', 'mkr', 'compound', 'comp', 'curve', 'crv', '1inch', 'enjin', 'enj',
        'basic attention token', 'bat', 'zcash', 'zec', 'dash', 'nexo', 'fantom', 'ftm',
        'the graph', 'grt'
    ]
    
    crypto_terms = [
        'crypto', 'cryptocurrency', 'blockchain', 'defi', 'nft', 'nfts',
        'altcoin', 'altcoins', 'token', 'tokens', 'coin', 'coins',
        'wallet', 'exchange', 'trading', 'hodl', 'hodling', 'moon', 'moonshot',
        'pump', 'dump', 'bull', 'bear', 'bullish', 'bearish', 'fomo', 'fud',
        'whale', 'whales', 'diamond hands', 'paper hands', 'to the moon',
        'lambo', 'lambos', 'wen moon', 'wen lambo', 'gm', 'gn', 'wagmi',
        'ngmi', 'dyor', 'ape', 'aping', 'rug', 'rugpull', 'scam', 'scams'
    ]
    
    if any(coin in text_lower for coin in specific_coins):
        return True
    
    if any(term in text_lower for term in crypto_terms):
        return True
    
    if '$' in text_lower and len(text_lower.split('$')[1].split()[0]) <= 10:
        return True
    
    return False

def extract_crypto_mention(text):
    text_lower = text.lower()
    dollar_symbols = re.findall(r'\$([a-z]{2,10})', text_lower)
    if dollar_symbols:
        return dollar_symbols[0].upper()
    
    coin_mappings = {
        'bitcoin': 'BITCOIN', 'btc': 'BITCOIN',
        'ethereum': 'ETHEREUM', 'eth': 'ETHEREUM',
        'solana': 'SOLANA', 'sol': 'SOLANA',
        'cardano': 'CARDANO', 'ada': 'CARDANO',
        'binance': 'BINANCE', 'bnb': 'BINANCE',
        'ripple': 'RIPPLE', 'xrp': 'RIPPLE',
        'polygon': 'POLYGON', 'matic': 'POLYGON',
        'avalanche': 'AVALANCHE', 'avax': 'AVALANCHE',
        'chainlink': 'CHAINLINK', 'link': 'CHAINLINK',
        'polkadot': 'POLKADOT', 'dot': 'POLKADOT',
        'litecoin': 'LITECOIN', 'ltc': 'LITECOIN',
        'uniswap': 'UNISWAP', 'uni': 'UNISWAP',
        'dogecoin': 'DOGECOIN', 'doge': 'DOGECOIN',
        'shiba': 'SHIBA', 'shib': 'SHIBA',
        'tron': 'TRON', 'trx': 'TRON',
        'stellar': 'STELLAR', 'xlm': 'STELLAR',
        'cosmos': 'COSMOS', 'atom': 'COSMOS',
        'near': 'NEAR',
        'algorand': 'ALGORAND', 'algo': 'ALGORAND',
        'tezos': 'TEZOS', 'xtz': 'TEZOS',
        'filecoin': 'FILECOIN', 'fil': 'FILECOIN',
        'hedera': 'HEDERA', 'hbar': 'HEDERA',
        'vechain': 'VECHAIN', 'vet': 'VECHAIN',
        'quant': 'QUANT', 'qnt': 'QUANT',
        'internet computer': 'ICP', 'icp': 'ICP',
        'aptos': 'APTOS', 'apt': 'APTOS',
        'flow': 'FLOW',
        'axie': 'AXIE', 'axs': 'AXIE',
        'decentraland': 'DECENTRALAND', 'mana': 'DECENTRALAND',
        'sandbox': 'SANDBOX', 'sand': 'SANDBOX',
        'aave': 'AAVE',
        'maker': 'MAKER', 'mkr': 'MAKER',
        'compound': 'COMPOUND', 'comp': 'COMPOUND',
        'curve': 'CURVE', 'crv': 'CURVE',
        '1inch': '1INCH',
        'enjin': 'ENJIN', 'enj': 'ENJIN',
        'basic attention token': 'BAT', 'bat': 'BAT',
        'zcash': 'ZCASH', 'zec': 'ZCASH',
        'dash': 'DASH',
        'nexo': 'NEXO',
        'fantom': 'FANTOM', 'ftm': 'FANTOM',
        'the graph': 'GRAPH', 'grt': 'GRAPH'
    }
    
    for coin_key, coin_name in coin_mappings.items():
        if coin_key in text_lower:
            return coin_name
    
    if 'defi' in text_lower:
        return 'DEFI'
    elif 'nft' in text_lower or 'nfts' in text_lower:
        return 'NFT'
    elif 'web3' in text_lower:
        return 'WEB3'
    
    return None

def scrape_news_sites():
    try:
        news_sites = [
            'https://www.coindesk.com/',
            'https://cointelegraph.com/',
            'https://bitcoinist.com/',
            'https://cryptonews.com/',
            'https://decrypt.co/',
            'https://www.coinbase.com/blog',
            'https://blog.chain.link/',
            'https://blog.uniswap.org/',
            'https://blog.polygon.technology/',
            'https://blog.avax.network/',
            'https://solana.com/news',
            'https://cardano.org/news',
            'https://polkadot.network/blog',
            'https://near.org/blog',
            'https://algorand.com/news',
            'https://tezos.com/news',
            'https://filecoin.io/blog',
            'https://hedera.com/blog',
            'https://vechain.org/news',
            'https://quant.network/news'
        ]
        
        all_headlines = []
        
        for site_url in news_sites:
            try:
                response = requests.get(site_url, timeout=10)
                response.raise_for_status()
                
                soup = BeautifulSoup(response.content, 'html.parser')
                
                headlines = soup.find_all(['h1', 'h2', 'h3'], limit=20)
                
                for headline in headlines:
                    text = headline.get_text(strip=True)
                    
                    if text and len(text) > 10 and is_crypto_related(text):
                        mentioned_crypto = extract_crypto_mention(text)
                        
                        if mentioned_crypto is None:
                            continue
                        
                        sentiment = enhanced_sentiment_analysis(text)
                        
                        site_name = site_url.replace('https://', '').replace('http://', '').split('/')[0]
                        if 'www.' in site_name:
                            site_name = site_name.replace('www.', '')
                        site_name = site_name.replace('.com', '').replace('.org', '').replace('.co', '').replace('.io', '')
                        site_name = site_name.replace('-', ' ').replace('_', ' ').title()
                        
                        all_headlines.append({
                            'platform': site_name,
                            'source': site_url,
                            'headline': text,
                            'full_text': text,
                            'sentiment': sentiment,
                            'mentioned_crypto': mentioned_crypto,
                            'timestamp': datetime.now().isoformat()
                        })
                        
                        if len(all_headlines) >= 50:  # Limit total
                            break
                            
            except Exception as e:
                logger.error(f"Error scraping news site {site_url}: {str(e)}")
                continue
        
        return all_headlines[:50]
        
    except Exception as e:
        logger.error(f"Error scraping news sites: {str(e)}")
        return []

def scrape_crypto_forums():
    try:
        forums = [
            'https://bitcointalk.org/',
            'https://cryptocurrencytalk.com/',
            'https://www.cryptocompare.com/forum/'
        ]
        
        forum_content = []
        
        for forum_url in forums:
            try:
                response = requests.get(forum_url, timeout=10)
                response.raise_for_status()
                
                soup = BeautifulSoup(response.content, 'html.parser')
                
                posts = soup.find_all(['h3', 'h2', 'a'], limit=10)
                
                for post in posts:
                    text = post.get_text(strip=True)
                    
                    if text and len(text) > 20 and is_crypto_related(text):
                        mentioned_crypto = extract_crypto_mention(text)
                        
                        if mentioned_crypto is None:
                            continue
                        
                        sentiment = enhanced_sentiment_analysis(text)
                        
                        forum_name = forum_url.replace('https://', '').replace('http://', '').split('/')[0]
                        if 'www.' in forum_name:
                            forum_name = forum_name.replace('www.', '')
                        forum_name = forum_name.replace('.com', '').replace('.org', '').replace('.co', '').replace('.io', '')
                        forum_name = forum_name.replace('-', ' ').replace('_', ' ').title()
                        
                        forum_content.append({
                            'platform': forum_name,
                            'source': forum_url,
                            'content': text,
                            'full_text': text,
                            'sentiment': sentiment,
                            'mentioned_crypto': mentioned_crypto,
                            'timestamp': datetime.now().isoformat()
                        })
                        
                        if len(forum_content) >= 50:
                            break
                            
            except Exception as e:
                logger.error(f"Error scraping forum {forum_url}: {str(e)}")
                continue
        
        return forum_content[:50]
        
    except Exception as e:
        logger.error(f"Error scraping crypto forums: {str(e)}")
        return []


def scrape_additional_news_sources():
    try:
        additional_news_sites = [
            'https://www.coinbase.com/blog',
            'https://blog.chain.link/',
            'https://blog.uniswap.org/',
            'https://blog.polygon.technology/',
            'https://blog.avax.network/'
        ]
        
        news_content = []
        
        for site_url in additional_news_sites[:3]:  # Limit to 3 sites
            try:
                headers = {
                    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
                    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
                    'Accept-Language': 'en-US,en;q=0.9'
                }
                
                response = requests.get(site_url, headers=headers, timeout=10)
                response.raise_for_status()
                
                soup = BeautifulSoup(response.content, 'html.parser')
                
                headlines = soup.find_all(['h1', 'h2', 'h3', 'h4'], limit=10)
                
                for headline in headlines:
                    text = headline.get_text(strip=True)
                    
                    if text and len(text) > 10 and is_crypto_related(text):
                        mentioned_crypto = extract_crypto_mention(text)
                        
                        if mentioned_crypto is None:
                            continue
                        
                        sentiment = enhanced_sentiment_analysis(text)
                        
                        site_name = site_url.replace('https://', '').replace('http://', '').split('/')[0]
                        if 'www.' in site_name:
                            site_name = site_name.replace('www.', '')
                        site_name = site_name.replace('.com', '').replace('.org', '').replace('.co', '').replace('.io', '').replace('.technology', '')
                        site_name = site_name.replace('-', ' ').replace('_', ' ').title()
                        
                        news_content.append({
                            'platform': site_name,
                            'source': site_url,
                            'headline': text,
                            'full_text': text,
                            'sentiment': sentiment,
                            'mentioned_crypto': mentioned_crypto,
                            'timestamp': datetime.now().isoformat()
                        })
                        
                        if len(news_content) >= 15:
                            break
                
                logger.info(f"Scraped {len(news_content)} items from additional news site")
                
            except Exception as e:
                logger.error(f"Error scraping additional news site {site_url}: {str(e)}")
                continue
        
        logger.info(f"Total additional news content scraped: {len(news_content)} items")
        return news_content
        
    except Exception as e:
        logger.error(f"Error in scrape_additional_news_sources: {str(e)}")
        return []

def main():
    try:
        logger.info("Starting scraping...")
        
        all_content = []
        
        platforms = [
            scrape_news_sites,
            scrape_crypto_forums,
            scrape_additional_news_sources
        ]
        
        for platform_scraper in platforms:
            try:
                content = platform_scraper()
                all_content.extend(content)
                logger.info(f"Scraped {len(content)} items from {platform_scraper.__name__}")
                time.sleep(2)
            except Exception as e:
                logger.error(f"Error scraping {platform_scraper.__name__}: {str(e)}")
                continue
        
        sqs = boto3.client('sqs', region_name='us-east-2')
        queue_url = 'https://sqs.us-east-2.amazonaws.com/203129030113/crypto-sentiment-processing-queue'
        
        messages_sent = 0
        for item in all_content:
            try:
                sqs.send_message(
                    QueueUrl=queue_url,
                    MessageBody=json.dumps(item)
                )
                messages_sent += 1
            except Exception as e:
                logger.error(f"Error sending message to SQS: {str(e)}")
                continue
        
        logger.info(f"Sent {messages_sent} messages to SQS")
        
        return {
            'statusCode': 200,
            'body': json.dumps(f'Processed {len(all_content)} items, sent {messages_sent} messages to SQS')
        }
        
    except Exception as e:
        logger.error(f"Error in scraper: {str(e)}")
        return {
            'statusCode': 500,
            'body': json.dumps(f'Error: {str(e)}')
        }

def lambda_handler(event, context):
    return main()

if __name__ == "__main__":
    main()