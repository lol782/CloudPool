use clap::{Parser, Subcommand};
use cloudpool_rust::file_service::FileService;

#[derive(Parser)]
#[command(name = "cloudpool")]
#[command(about = "CloudPool Native CLI Services", long_about = None)]
struct Cli {
    #[command(subcommand)]
    command: Commands,
}

#[derive(Subcommand)]
enum Commands {
    /// Calculate the SHA-256 checksum of a string
    Checksum {
        /// Input string
        input: String,
    },
    /// Compress a string
    Compress {
        /// Input string
        input: String,
    },
}

fn main() {
    let cli = Cli::parse();

    match &cli.command {
        Commands::Checksum { input } => {
            match FileService::calculate_checksum(input.as_bytes()) {
                Ok(checksum) => println!("{}", checksum),
                Err(e) => eprintln!("Error: {}", e),
            }
        }
        Commands::Compress { input } => {
            match FileService::compress(input.as_bytes()) {
                Ok(compressed) => println!("Compressed size: {} bytes", compressed.len()),
                Err(e) => eprintln!("Error: {}", e),
            }
        }
    }
}
