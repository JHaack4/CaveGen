import java.util.*;
import java.math.BigInteger;

public class Seed {

    public static void main(String[] args) {
        new Seed().run(args);
    }

    String helpString = "Usage:\n  Seed nth n\n  Seed nthinv seed\n  Seed dist seed1 seed2\n  Seed next seed [n]\n  Seed seed2seq seed\n  Seed seq2seed seq\n  Seed ieee hex\n" +
        "  python videodigits.py - read the newest video in seed_video_path folder, and parse the digits on the result screen\n" +
        "  Seed chresult - read from seed_digits_parsed and infer the seed\n" +
        "  Seed caveviewer [n] - read from last_known_seed and open cave viewer to see reachable seeds. (Press s to select seed)\n" +
        "  Seed timer - create a countdown timer to reach the closest seed in seed_desired";
    //Long.decode(args[++i]).longValue()
    void run(String args[]) {
        try {
            if (args.length == 0) {
                System.out.println(helpString);
            } else if (args[0].equalsIgnoreCase("nth") && args.length >= 2) {
                System.out.println(Long.toHexString(nth(Long.parseLong(args[1]))));
            } else if (args[0].equalsIgnoreCase("nthinv") && args.length >= 2) {
                System.out.println(nth_inv(Long.decode(args[1])));
            } else if (args[0].equalsIgnoreCase("dist") && args.length >= 3) {
                System.out.println(dist(Long.decode(args[1]), Long.decode(args[2])));
            } else if (args[0].equalsIgnoreCase("next") && args.length >= 3) {
                System.out.println(Long.toHexString(next_seed(Long.decode(args[1]), Long.decode(args[2]))));
            } else if (args[0].equalsIgnoreCase("next") && args.length >= 2) {
                System.out.println(Long.toHexString(next_seed(Long.decode(args[1]))));
            } else if (args[0].equalsIgnoreCase("seq2seed") && args.length >= 2) {
                ArrayList<Integer> r = sequence_to_seed(args[1]);
                for (Integer i: r) System.out.println(Long.toHexString(i));
            }  else if (args[0].equalsIgnoreCase("seed2seq") && args.length >= 3) {
                System.out.println(seed_to_sequence(Long.decode(args[1]), Integer.parseInt(args[2])));
            } else if (args[0].equalsIgnoreCase("seed2seq") && args.length >= 2) {
                System.out.println(seed_to_sequence(Long.decode(args[1]), 30));
            } else if (args[0].equalsIgnoreCase("ieee") && args.length >= 2) {
                if (args[1].length() == 8) {
                    System.out.println(Float.intBitsToFloat(Integer.decode("0x" + args[1])));
                }
                if (args[1].length() == 16) {
                    System.out.println(Double.longBitsToDouble(Long.decode("0x" + args[1])));
                }
            } 
            else if (args[0].equalsIgnoreCase("test")) {
                runTests();
            } 
            else if (args[0].equalsIgnoreCase("chresult")) {
                
            } 
            else if (args[0].equalsIgnoreCase("caveviewer")) {

            } 
            else if (args[0].equalsIgnoreCase("timer")) {

            } 
            else {
                System.out.println(helpString);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("\n" + helpString);
        }
    }
	
	void runTests() {

		System.out.println(seed_to_sequence(1227587417, 15));
		
		System.out.println("method LLL");
		ArrayList<Integer> seed1 = sequence_to_seed("111111711");
		for (int i: seed1) System.out.println(i + " " + seed_to_sequence(i,20));
		System.out.println("slow method");
		ArrayList<Integer> seedL = sequence_to_seed_slow("111111711");
		for (int i: seedL) System.out.println(i + " " + seed_to_sequence(i,20));
	
		long[] tests = new long[] {0, 1, 10, 25, 60, 65535, 65536, C-1, C, C+1, A-1, A, A+1, M/2-1, M/2, M/2+1, M-2, M-1};
	
		for (long i: tests) {
			long x = nth(i);
			System.out.println(i + ": " + x + " -> " + nth_inv(x) + " " + nth_inv2(x) + " " + nth_inv3(x));
		}
    }
    
    // -------------------- Functional Seed Code ------------------------------

	final long A = 0x41c64e6d;
	final long C = 0x3039;
	final long M = 0x100000000L;
    
    long next_seed(long seed) {
		return (A*seed+C) % M;
	}
	
	long a_inv = inverse(A,M);
	long prev_seed(long seed) {
		return ((seed-C) * a_inv) % M;
    }
    
    long next_seed(long seed, long n) {
        long idx = nth_inv(seed);
        idx += n;
        if (idx < 0) idx += (M * (1-idx/M)) % M;
        return nth(idx);
    }

    // Compute the nth seed in O(log(M)) time
    private long r1_ = (C * inverse((A - 1)/4, M)) % M;
	long nth(long n) {
		n = n % M;
		long r2 = power(A,n,4*M)/4;
		return (r1_ * r2) % M;
    }
    
    // The 2-adic valuation of x
	// aka the largest integer v such that 2^v divides x
	int v2(long x) {
		return x == 0 ? Integer.MAX_VALUE : Long.numberOfTrailingZeros(x);
	}
	
	// find the value of n such that the nth seed is x
	// Technique is based on Mihai's lemma / lifting the exponent
	long nth_inv(long x) {
		long xpow = (x * (A-1) * inverse(C,M) + 1) % (4*M);
		long n=0, p=1;
		for (int i = 0; i < 32; i++) {
			if ( v2(power(A, n+p, 4*M) - xpow) > v2(power(A, n, 4*M) - xpow) )
				n += p;
			p *= 2;
		}
		return n;
	}
	
	BigInteger theta(long num) {
		if (num % 4 == 3) {
            num = 4*M - num;
        }
        BigInteger xhat = BigInteger.valueOf(num);
        xhat = xhat.modPow(BigInteger.ONE.shiftLeft(32+1), BigInteger.ONE.shiftLeft(2*32+3));
        xhat = xhat.subtract(BigInteger.ONE);
        xhat = xhat.divide(BigInteger.ONE.shiftLeft(32+3));
        xhat = xhat.mod(BigInteger.ONE.shiftLeft(32));
        return xhat;
	}
	
	// find the value of n such that the nth seed is x
	long nth_inv2(long x) {
		BigInteger thetaAInverse = BigInteger.valueOf(2755579993L); // inverse(theta(A), M)
		long xpow = (x * (A-1) * inverse(C,M) + 1) % (4*M);
		return thetaAInverse.multiply(theta(xpow)).mod(bigInt(M)).longValue();
    }
    
    // find the value of n such that the nth seed is x
	// the runtime and memory is O(sqrt(M))
	HashMap<Long,Long> table = null;
	long nth_inv3(long x) {
		x = x % M;
		long m = (long)(Math.sqrt(M));
		if (table == null) {
			table = new HashMap<Long, Long>();
			for (long i = 0; i < M; i += m) {
				table.put(nth(i), i);
			}
		}
		long r = x;
		for (long i = 0; i < m; i++) {
			if (table.containsKey(r)) {
				return (table.get(r) + M - i) % M;
			}
			r = (A * r + C) % M;
		}
		return -1;
	}
	
	// distance from a1 to a2 (i.e. how many advances from a1 to a2)
	long dist(long a1, long a2) {
        long x = nth_inv(a2) - nth_inv(a1);
        if (x < 0) x += ((1-x/M) * M) % M;
		return x % M;
	}
	
	long power(long b, long e, long m) { 
		return bigInt(b).modPow(bigInt(e), bigInt(m)).longValue();
    } 
	
	long inverse(long a, long m) {
		return bigInt(a).modInverse(bigInt(m)).longValue();
	}
	
	long gcd(long a, long b) {
		return bigInt(a).gcd(bigInt(b)).longValue();
	}
	
	BigInteger bigInt(long l) {
		return BigInteger.valueOf(l);
	}
	
	int digitObservation(long seed) {
		return (int)((seed >> 16) & 0x7fff) * 9 / 32768 + 1;
	}
	
	String seed_to_sequence(long seed, int length) {
		String s = "";
		for (int i = 0; i < length; i++) {
			s += digitObservation(seed);
			seed = next_seed(seed);
		}
		return s;
    }
    
    // Pre-computed LLL matrices in the 10 length case.
	long[] lattice_P = {0L, 12345L, 1406932606L, 654583775L, 1449466924L, 229283573L, 1109335178L, 1051550459L, 1293799192L, 794471793L};
	long[][] lattice_LLL = {
			{7285528L, -83449544L, 45423832L, -50915336L, 5424280L, 11797688L, 73798232L, 18653048L, 9860632L, 110860344L},
			{-106217756L, 57360148L, -10882172L, -40442060L, -102086364L, 14818388L, 31434692L, -152033676L, 28292964L, -49705580L},
			{34283127L, 151927467L, -63892273L, -61411805L, -6025497L, 51296859L, 124719807L, 12266835L, -122448297L, -91257589L},
			{52786254L, 45500726L, 128950270L, 83526438L, 134441774L, 129017494L, 117219806L, 43421574L, 24768526L, 14907894L},
			{-167644423L, -59616763L, 109605409L, 23373325L, -41899639L, 208006485L, -93064399L, 6126045L, 14432537L, -109056603L},
			{-57263749L, 26997599L, 235377011L, -109316105L, -10496469L, -11449777L, 39499171L, 50921575L, -6386213L, -132308929L},
			{12218042L, -84291278L, -121156022L, -34414206L, 229351514L, -58019246L, 114223338L, 817058L, -82949126L, -40336014L},
			{61125926L, -38357714L, 26421398L, -66454562L, 112320902L, 1563150L, 57416694L, -70198338L, 232092646L, -149525266L},
			{-80092662L, -130004414L, -83117542L, 13510930L, -29951830L, -89674654L, 95830458L, 108832818L, 117365066L, -101511038L},
			{-6443794L, 250029398L, -61924962L, -173011898L, 64392654L, -52222794L, -55962242L, 74156710L, 141335726L, 177191446L}	
	};
	long[][] lattice_LLLinv = {
			{3L, -8L, 5L, -3L, -3L, -2L, -5L, 6L, -4L, -3L},
			{-7L, 3L, 1L, 4L, -2L, 1L, -1L, -2L, 0L, 3L},
			{0L, 1L, -4L, 3L, -2L, 6L, -1L, -1L, -2L, -1L},
			{-9L, 3L, -5L, 8L, -3L, -2L, -1L, -4L, 3L, -2L},
			{-4L, 0L, -4L, 3L, 1L, 1L, 7L, 0L, -2L, 2L},
			{6L, -3L, 6L, 0L, 7L, -5L, -2L, 3L, -2L, 0L},
			{5L, 5L, 3L, 5L, -3L, 0L, 0L, -1L, 4L, -1L},
			{0L, -8L, 3L, 0L, 2L, 1L, -2L, -2L, 5L, 2L},
			{0L, 1L, -2L, 2L, 0L, -2L, -3L, 4L, 3L, 2L},
			{6L, 2L, -3L, 2L, -1L, -2L, 0L, -3L, -1L, 2L}
	};

	// Take an ordered sequence of digit observations and output the potential seeds
	// length of sequence should be 10 or more, ideally, to avoid collisions
	// This is based on Matthew's implementation of lattice reduction using the LLL algorithm.
	ArrayList<Integer> sequence_to_seed(String sequence) {
		
		// convert the sequence to an array.
		long[] seq = new long[sequence.length()];
		for (int i = 0; i < sequence.length(); i++) {
			seq[i] = sequence.charAt(i) - '0';
		}
		
		// find the upper and lower bounds for the seed
		// for each part of the sequence
		int N = 10;
		long[] LowerBounds = new long[N];
		long[] UpperBounds = new long[N];
		for (int i = 0; i < N; i++) {
			if (i < seq.length) {
				LowerBounds[i] = (seq[i]-1) * 0x10000 * (32768/9 + 1);
				UpperBounds[i] = seq[i] * 0x10000 * (32768/9 + 1);
			} else {
				LowerBounds[i] = 0;
				UpperBounds[i] = Integer.MAX_VALUE;
			}
		}
		
		// find upper and lower bounds in the LLL basis representation
		// this is the crucial step which reduces the number of vectors we must check
		double[] minD = new double[N];
		double[] maxD = new double[N];
		double M2_31 = M / 2;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (lattice_LLLinv[i][j] < 0) {
					minD[j] += (UpperBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
					maxD[j] += (LowerBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
				} else {
					maxD[j] += (UpperBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
					minD[j] += (LowerBounds[i] - lattice_P[i]) * lattice_LLLinv[i][j] / M2_31;
				}
			}
		}
		// clamp these to integer values
		long[] min = new long[N];
		long[] max = new long[N];
		for (int i = 0; i < N; i++) {
			min[i] = (long)Math.ceil(minD[i]);
			max[i] = (long)Math.floor(maxD[i]);
		}
		
		// v is the vector of seeds that we are currently checking (initially min * LLL + P)
		// b is the vector v but in the basis representation (initially "min")
		long[] v = new long[N];
		long[] b = new long[N];
		for (int i = 0; i < N; i++) {
			b[i] = min[i];
			v[i] = lattice_P[i];
			for (int j = 0; j < N; j++)
				v[i] += min[j]*lattice_LLL[j][i];
		}

		// iterate over the set of possibilities contained in min/max. (we're checking all of them)
		ArrayList<Integer> candidates = new ArrayList<Integer>();		
		while(true) {

			// check if the current v vector falls in the desired region
			boolean isInRegion = true;
			for (int i = 0; i < N; i++) {
		        if (v[i] < LowerBounds[i] || v[i] > UpperBounds[i]) {
		            isInRegion = false;
		            break;
		        }
			}
			if (isInRegion) {
				candidates.add((int)(v[0] & 0x7fffffff));
			}
			
			// move to the next b/v vector.
			boolean done = true;
			for (int i = 0; i < N; i++) {
		        b[i] += 1;
		        for (int j = 0; j < N; j++)
		        	v[j] += lattice_LLL[i][j];
		        if (b[i] > max[i]) {
		            b[i] = min[i];
		            for (int j = 0; j < v.length; j++)
		            	v[j] -= lattice_LLL[i][j]*(max[i]-min[i]+1);
		        }
		        else {
		        	done = false;
		            break;
		        }
			}
			if (done)
				break;
		}
		
		// Verify the candidates and return the ones that match
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int i = 0; i < candidates.size(); i++) {
			if (seed_to_sequence(candidates.get(i), sequence.length()).equals(sequence))
				ret.add(candidates.get(i));
		}
		
		return candidates;
	}
	
        
    // ---------- slow versions -----------

    long nth_slow(long n) {
		long ret = 0;
		for (long i = 0; i < n; i++) {
			ret = (A * ret + C) % M;
		}
		return ret;
	}
	
	long nth_inv_slow(long a) {
		a = a % M;
		long cnt = 0;
		long r = 0;
		while (r != a) {
			r = (A * r + C) % M;
			cnt++;
		}
		return cnt;
    }
    
	long dist_slow(long a1, long a2) {
		return (nth_inv_slow(a2) - nth_inv_slow(a1) + M) % M;
	}
	
	ArrayList<Integer> sequence_to_seed_slow(String sequence) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		
		long[] seq = new long[sequence.length()];
		for (int i = 0; i < sequence.length(); i++) {
			seq[i] = sequence.charAt(i) - '0';
        }
        
		outer:
		for (int i = 0; i >= 0; i++) { // i from 0 to 2^31 - 1
			long seed = i;
			for (int j = 0; j < seq.length; j++) {
				if (digitObservation(seed) != seq[j])
					continue outer;
				seed = (A*seed+C) % M;
			}
			ret.add(i);
		}
		
		return ret;
    }
    

    
}