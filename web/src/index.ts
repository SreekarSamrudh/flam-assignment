class FrameViewer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private fpsDisplay: HTMLElement;
    private resolutionDisplay: HTMLElement;
    private frameCount: number = 0;
    private lastFpsTime: number = Date.now();
    private currentFps: number = 0;

    constructor() {
        this.canvas = document.getElementById('canvas') as HTMLCanvasElement;
        this.ctx = this.canvas.getContext('2d')!;
        this.fpsDisplay = document.getElementById('fpsDisplay')!;
        this.resolutionDisplay = document.getElementById('resolutionDisplay')!;

        this.setupControls();
        this.updateStats();
    }

    private setupControls(): void {
        const loadBtn = document.getElementById('loadSampleBtn')!;
        const clearBtn = document.getElementById('clearBtn')!;
        const fileInput = document.getElementById('fileInput') as HTMLInputElement;
        const uploadBtn = document.getElementById('uploadBtn')!;

        loadBtn.addEventListener('click', () => {
            this.loadSampleImage();
        });

        clearBtn.addEventListener('click', () => {
            this.clearCanvas();
        });

        uploadBtn.addEventListener('click', () => {
            fileInput.click();
        });

        fileInput.addEventListener('change', (event: Event) => {
            const target = event.target as HTMLInputElement;
            if (target.files && target.files.length > 0) {
                this.loadUserImage(target.files[0]);
            }
        });
    }

    private loadUserImage(file: File): void {
        const reader = new FileReader();
        reader.onload = (e: ProgressEvent<FileReader>) => {
            if (e.target?.result) {
                const base64String = (e.target.result as string).split(',')[1];
                this.setImageBase64(base64String);
                this.simulateFps();
            }
        };
        reader.readAsDataURL(file);
    }

    private loadSampleImage(): void {
        const sampleBase64 = this.generateSampleEdgeImage();
        this.setImageBase64(sampleBase64);
        this.simulateFps();
    }

    private generateSampleEdgeImage(): string {
        const tempCanvas = document.createElement('canvas');
        tempCanvas.width = 640;
        tempCanvas.height = 480;
        const tempCtx = tempCanvas.getContext('2d')!;

        tempCtx.fillStyle = '#000000';
        tempCtx.fillRect(0, 0, 640, 480);

        tempCtx.strokeStyle = '#FFFFFF';
        tempCtx.lineWidth = 2;

        for (let i = 0; i < 50; i++) {
            const x1 = Math.random() * 640;
            const y1 = Math.random() * 480;
            const x2 = Math.random() * 640;
            const y2 = Math.random() * 480;
            
            tempCtx.beginPath();
            tempCtx.moveTo(x1, y1);
            tempCtx.lineTo(x2, y2);
            tempCtx.stroke();
        }

        for (let i = 0; i < 20; i++) {
            const x = Math.random() * 580 + 30;
            const y = Math.random() * 420 + 30;
            const radius = Math.random() * 50 + 20;
            
            tempCtx.beginPath();
            tempCtx.arc(x, y, radius, 0, Math.PI * 2);
            tempCtx.stroke();
        }

        for (let i = 0; i < 15; i++) {
            const x = Math.random() * 540 + 50;
            const y = Math.random() * 380 + 50;
            const width = Math.random() * 100 + 30;
            const height = Math.random() * 100 + 30;
            
            tempCtx.beginPath();
            tempCtx.rect(x, y, width, height);
            tempCtx.stroke();
        }

        return tempCanvas.toDataURL().split(',')[1];
    }

    public setImageBase64(base64Data: string): void {
        const img = new Image();
        img.onload = () => {
            this.canvas.width = img.width;
            this.canvas.height = img.height;
            this.ctx.drawImage(img, 0, 0);
            this.updateResolution(img.width, img.height);
            this.incrementFrameCount();
        };
        img.src = 'data:image/png;base64,' + base64Data;
    }

    private clearCanvas(): void {
        this.ctx.fillStyle = '#000000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        this.currentFps = 0;
        this.updateStats();
    }

    private incrementFrameCount(): void {
        this.frameCount++;
        const currentTime = Date.now();
        const elapsed = currentTime - this.lastFpsTime;

        if (elapsed >= 1000) {
            this.currentFps = this.frameCount * 1000 / elapsed;
            this.frameCount = 0;
            this.lastFpsTime = currentTime;
            this.updateStats();
        }
    }

    private simulateFps(): void {
        this.currentFps = 15 + Math.random() * 15;
        this.updateStats();
    }

    private updateResolution(width: number, height: number): void {
        this.resolutionDisplay.textContent = `Resolution: ${width}x${height}`;
    }

    private updateStats(): void {
        this.fpsDisplay.textContent = `FPS: ${this.currentFps.toFixed(1)}`;
        this.resolutionDisplay.textContent = `Resolution: ${this.canvas.width}x${this.canvas.height}`;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new FrameViewer();
});
